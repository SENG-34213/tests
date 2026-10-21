#!/usr/bin/env python3
"""Build a traceable register from JUnit XML and summarize imported JaCoCo XML."""

from __future__ import annotations

import argparse
import csv
import hashlib
import json
import re
import sys
import xml.etree.ElementTree as ET
from collections import defaultdict
from datetime import datetime, timezone
from pathlib import Path


METADATA_FIELDS = (
    "test_case_id",
    "requirement",
    "service",
    "scenario",
    "priority",
    "class_name",
    "method_name",
    "level",
    "required_environment",
    "expected_result",
)
REGISTER_FIELDS = (
    "test_case_id",
    "requirement",
    "service",
    "scenario",
    "priority",
    "test_class",
    "test_method",
    "level",
    "required_environment",
    "expected_result",
    "status",
    "skip_reason",
    "report_file",
    "run_id",
    "run_date_utc",
    "test_commit",
    "backend_commit",
    "mapping_status",
)
COUNTER_TYPES = ("LINE", "BRANCH", "INSTRUCTION", "METHOD", "COMPLEXITY")
SECRET_PATTERN = re.compile(
    r"(?i)(password|passwd|token|secret|authorization|api[_-]?key)(\s*[=: ]\s*)[^\s,;]+"
)
URL_CREDENTIAL_PATTERN = re.compile(r"(?i)(https?://)[^/@\s]+:[^/@\s]+@")
DOCUMENTED_CASE_ID = re.compile(r"^(?:TC-)?[A-Z0-9]+(?:-[A-Z0-9]+)*-\d{2,}$")


def safe_text(value: str | None) -> str:
    value = value or ""
    value = URL_CREDENTIAL_PATTERN.sub(r"\1[REDACTED]@", value)
    return SECRET_PATTERN.sub(r"\1\2[REDACTED]", value)


def display_path(path: Path) -> str:
    try:
        return path.resolve().relative_to(Path.cwd().resolve()).as_posix()
    except ValueError:
        return path.name


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--junit-dir",
        action="append",
        type=Path,
        default=[],
        help="JUnit XML file or directory (repeat for Surefire and Failsafe).",
    )
    parser.add_argument(
        "--jacoco-dir",
        action="append",
        type=Path,
        default=[],
        help="Imported JaCoCo XML file or directory (repeat as needed).",
    )
    parser.add_argument(
        "--metadata",
        type=Path,
        default=Path("docs/test-case-mappings.csv"),
        help="CSV mapping maintained test IDs to JUnit class and method names.",
    )
    parser.add_argument(
        "--case-docs-dir",
        type=Path,
        default=Path("docs"),
        help="Directory containing detailed *-test-cases.md files.",
    )
    parser.add_argument(
        "--output-dir", type=Path, default=Path("target/test-evidence")
    )
    return parser.parse_args()


def read_metadata(path: Path) -> list[dict[str, str]]:
    if not path.exists():
        raise ValueError(f"Metadata CSV does not exist: {path}")
    with path.open(newline="", encoding="utf-8-sig") as source:
        reader = csv.DictReader(source)
        missing = set(METADATA_FIELDS) - set(reader.fieldnames or [])
        if missing:
            raise ValueError("Metadata CSV is missing columns: " + ", ".join(sorted(missing)))
        rows = []
        seen_ids: set[str] = set()
        for line_number, raw in enumerate(reader, start=2):
            row = {key: (raw.get(key) or "").strip() for key in METADATA_FIELDS}
            if not row["test_case_id"]:
                raise ValueError(f"Metadata row {line_number} has no test_case_id")
            if row["test_case_id"] in seen_ids:
                raise ValueError(f"Duplicate test_case_id on row {line_number}: {row['test_case_id']}")
            if not row["class_name"] or not row["method_name"]:
                raise ValueError(
                    f"Metadata row {line_number} must map {row['test_case_id']} to class_name and method_name"
                )
            seen_ids.add(row["test_case_id"])
            rows.append(row)
        return rows


def documented_cases(directory: Path) -> list[dict[str, str]]:
    cases: dict[str, dict[str, str]] = {}
    if not directory.is_dir():
        return []
    for path in sorted(directory.glob("*-test-cases.md")):
        headers: list[str] = []
        for line in path.read_text(encoding="utf-8").splitlines():
            if not line.startswith("|"):
                continue
            cells = [cell.strip() for cell in line.strip().strip("|").split("|")]
            if cells and cells[0].lower() in {"id", "test case id", "case id"}:
                headers = [cell.lower() for cell in cells]
                continue
            if len(cells) < 3 or not DOCUMENTED_CASE_ID.fullmatch(cells[0]):
                continue
            case_id = cells[0]
            scenario_index = next(
                (i for i, header in enumerate(headers) if "scenario" in header or "name" in header),
                1,
            )
            expected_index = next(
                (
                    i
                    for i, header in enumerate(headers)
                    if ("expected" in header or "outcome" in header)
                    and i < len(cells)
                ),
                -1,
            )
            cases[case_id] = {
                "test_case_id": case_id,
                "requirement": "UNMAPPED",
                "service": path.name.removesuffix("-test-cases.md"),
                "scenario": cells[scenario_index] if scenario_index < len(cells) else cells[1],
                "priority": "",
                "class_name": "",
                "method_name": "",
                "level": "Documented",
                "required_environment": "",
                "expected_result": cells[expected_index] if expected_index >= 0 else "",
                "documentation": path.as_posix(),
            }
    return list(cases.values())


def junit_outcomes(paths: list[Path]) -> tuple[dict[str, dict[str, str]], list[str]]:
    outcomes: dict[str, dict[str, str]] = {}
    errors: list[str] = []
    severity = {"Passed": 0, "Skipped": 1, "Failed": 2}
    for path in paths:
        try:
            root = ET.parse(path).getroot()
        except (ET.ParseError, OSError) as error:
            errors.append(f"{path}: invalid JUnit XML ({error})")
            continue
        for case in root.iter("testcase"):
            class_name = (case.get("classname") or "").strip()
            method_name = (case.get("name") or "").strip()
            if not class_name or not method_name:
                errors.append(f"{path}: testcase without classname or name")
                continue
            method_key = method_name.split("[", 1)[0]
            status = "Passed"
            skip_reason = ""
            failed = case.find("failure") is not None or case.find("error") is not None
            skipped = case.find("skipped")
            if failed:
                status = "Failed"
            elif skipped is not None:
                status = "Skipped"
                skip_reason = safe_text(skipped.get("message") or "Skipped; see JUnit report")
            key = f"{class_name}#{method_key}"
            outcome = {
                "status": status,
                "skip_reason": skip_reason,
                "report_file": display_path(path),
            }
            if key not in outcomes or severity[status] > severity[outcomes[key]["status"]]:
                outcomes[key] = outcome
    return outcomes, errors


def build_register(
    metadata: list[dict[str, str]],
    documented: list[dict[str, str]],
    outcomes: dict[str, dict[str, str]],
) -> list[dict[str, str]]:
    run_id = os_value("GITHUB_RUN_ID", "local")
    run_date = datetime.now(timezone.utc).isoformat(timespec="seconds")
    test_commit = os_value("GITHUB_SHA", "unknown")
    backend_commit = os_value("BACKEND_COMMIT_SHA", "unknown")
    rows: list[dict[str, str]] = []
    mapped_keys: set[str] = set()
    metadata_ids = {item["test_case_id"] for item in metadata}
    for item in documented:
        if item["test_case_id"] in metadata_ids:
            continue
        rows.append(
            {
                "test_case_id": item["test_case_id"],
                "requirement": item["requirement"],
                "service": item["service"],
                "scenario": item["scenario"],
                "priority": "",
                "test_class": "",
                "test_method": "",
                "level": item["level"],
                "required_environment": "",
                "expected_result": item["expected_result"],
                "status": "Not Mapped",
                "skip_reason": "No JUnit class/method mapping exists for this documented case.",
                "report_file": item["documentation"],
                "run_id": run_id,
                "run_date_utc": run_date,
                "test_commit": test_commit,
                "backend_commit": backend_commit,
                "mapping_status": "Documented only",
            }
        )
    for item in metadata:
        key = f"{item['class_name']}#{item['method_name']}"
        mapped_keys.add(key)
        result = outcomes.get(key)
        rows.append(
            {
                "test_case_id": item["test_case_id"],
                "requirement": item["requirement"],
                "service": item["service"],
                "scenario": item["scenario"],
                "priority": item["priority"],
                "test_class": item["class_name"],
                "test_method": item["method_name"],
                "level": item["level"],
                "required_environment": item["required_environment"],
                "expected_result": item["expected_result"],
                "status": result["status"] if result else "Not Run",
                "skip_reason": result["skip_reason"] if result else "",
                "report_file": result["report_file"] if result else "",
                "run_id": run_id,
                "run_date_utc": run_date,
                "test_commit": test_commit,
                "backend_commit": backend_commit,
                "mapping_status": "Mapped",
            }
        )
    for key, result in sorted(outcomes.items()):
        if key in mapped_keys:
            continue
        class_name, method_name = key.rsplit("#", 1)
        digest = hashlib.sha256(key.encode("utf-8")).hexdigest()[:12]
        rows.append(
            {
                "test_case_id": f"UNMAPPED-{digest}",
                "requirement": "UNMAPPED",
                "service": "",
                "scenario": f"JUnit test {method_name}",
                "priority": "",
                "test_class": class_name,
                "test_method": method_name,
                "level": "",
                "required_environment": "",
                "expected_result": "Add an explicit test-case and requirement mapping.",
                "status": result["status"],
                "skip_reason": result["skip_reason"],
                "report_file": result["report_file"],
                "run_id": run_id,
                "run_date_utc": run_date,
                "test_commit": test_commit,
                "backend_commit": backend_commit,
                "mapping_status": "Unmapped",
            }
        )
    return rows


def jacoco_reports(paths: list[Path]) -> tuple[list[dict[str, object]], list[str]]:
    reports: list[dict[str, object]] = []
    errors: list[str] = []
    seen_modules: set[tuple[str, str]] = set()
    for path in paths:
        try:
            root = ET.parse(path).getroot()
            if root.tag != "report":
                raise ValueError("JaCoCo root element is not <report>")
            counters = {}
            for counter in root.findall("counter"):
                counter_type = counter.get("type", "")
                covered = int(counter.get("covered", "0"))
                missed = int(counter.get("missed", "0"))
                total = covered + missed
                counters[counter_type] = {
                    "covered": covered,
                    "missed": missed,
                    "total": total,
                    "percent": round(covered * 100 / total, 2) if total else None,
                }
            if "LINE" not in counters and "BRANCH" not in counters:
                raise ValueError("JaCoCo report has no line or branch counters")
        except (ET.ParseError, OSError, ValueError) as error:
            errors.append(f"{path}: invalid JaCoCo XML ({error})")
            continue
        path_parts = path.parts
        if "unit" in path_parts or any(part.startswith("jacoco-unit-") for part in path_parts):
            category = "unit"
        elif "integration" in path_parts or any(
            part.startswith("jacoco-integration-") for part in path_parts
        ):
            category = "integration"
        else:
            category = "imported"
        report_name = root.get("name") or path.parent.name
        module_key = (category, report_name)
        if module_key in seen_modules:
            errors.append(
                f"{path}: duplicate JaCoCo report for {category} module {report_name}"
            )
            continue
        seen_modules.add(module_key)
        reports.append(
            {
                "category": category,
                "report_name": report_name,
                "report_file": display_path(path),
                "counters": {name: counters[name] for name in COUNTER_TYPES if name in counters},
            }
        )
    return reports, errors


def os_value(name: str, default: str) -> str:
    import os

    return safe_text(os.environ.get(name, default))


def write_register(path: Path, rows: list[dict[str, str]]) -> None:
    with path.open("w", newline="", encoding="utf-8-sig") as target:
        writer = csv.DictWriter(target, fieldnames=REGISTER_FIELDS, extrasaction="ignore")
        writer.writeheader()
        writer.writerows(rows)


def markdown_register(rows: list[dict[str, str]]) -> str:
    counts: dict[str, int] = defaultdict(int)
    for row in rows:
        counts[row["status"]] += 1
    lines = [
        "# Executed Test Register",
        "",
        "Generated from JUnit XML and `docs/test-case-mappings.csv`.",
        "Unmapped JUnit methods are listed explicitly; they are not silently treated as traceable coverage.",
        "",
        "| Status | Count |",
        "|---|---:|",
    ]
    for status in ("Passed", "Failed", "Skipped", "Not Run", "Not Mapped"):
        lines.append(f"| {status} | {counts[status]} |")
    lines.extend(
        [
            "",
            "| Test case ID | Requirement | Service | Scenario | Test class/method | Level | Status | Mapping | Skip reason / evidence |",
            "|---|---|---|---|---|---|---|---|---|---|",
        ]
    )
    for row in rows:
        fields = (
            row["test_case_id"],
            row["requirement"],
            row["service"],
            row["scenario"],
            f"{row['test_class']}#{row['test_method']}",
            row["level"],
            row["status"],
            row["mapping_status"],
            row["skip_reason"] or row["report_file"],
        )
        lines.append("| " + " | ".join(value.replace("|", "\\|").replace("\n", " ") for value in fields) + " |")
    lines.append("")
    return "\n".join(lines)


def markdown_coverage(reports: list[dict[str, object]]) -> str:
    lines = [
        "# Backend Coverage Evidence",
        "",
        "Coverage is reported only for imported JaCoCo backend reports. This repository's API-test classes are not backend product coverage.",
        "Counters are aggregated across distinct modules within each artifact category. Unit and integration data stay separate.",
        "",
    ]
    if not reports:
        lines.extend(
            [
                "**Status: Unavailable.** No JaCoCo XML was supplied. This is not a 0% measurement and does not satisfy a backend coverage gate.",
                "Download the backend CI JaCoCo artifacts and rerun the documented evidence command to summarize them.",
                "",
            ]
        )
        return "\n".join(lines)
    lines.extend(
        [
            "| Category | Module count | Line coverage | Branch coverage | Instruction coverage |",
            "|---|---:|---:|---:|---:|",
        ]
    )
    categories: dict[str, list[dict[str, object]]] = defaultdict(list)
    for report in reports:
        categories[str(report["category"])].append(report)

    def aggregate(items: list[dict[str, object]]) -> dict[str, dict[str, object]]:
        totals: dict[str, dict[str, int]] = defaultdict(lambda: {"covered": 0, "missed": 0})
        for item in items:
            for counter_type, counter in item["counters"].items():
                totals[counter_type]["covered"] += int(counter["covered"])
                totals[counter_type]["missed"] += int(counter["missed"])
        result: dict[str, dict[str, object]] = {}
        for counter_type, values in totals.items():
            total = values["covered"] + values["missed"]
            result[counter_type] = {
                **values,
                "total": total,
                "percent": round(values["covered"] * 100 / total, 2) if total else None,
            }
        return result

    def percent(counters: dict[str, object], counter_type: str) -> str:
        counter = counters.get(counter_type)
        return f"{counter['percent']:.2f}%" if counter and counter["percent"] is not None else "n/a"

    for category, items in sorted(categories.items()):
        counters = aggregate(items)
        lines.append(
            f"| {category} aggregate | {len(items)} | {percent(counters, 'LINE')} | {percent(counters, 'BRANCH')} | {percent(counters, 'INSTRUCTION')} |"
        )
    for report in reports:
        counters = report["counters"]
        lines.append(
            f"| {report['category']}: {report['report_name']} | 1 | {percent(counters, 'LINE')} | {percent(counters, 'BRANCH')} | {percent(counters, 'INSTRUCTION')} |"
        )
    lines.extend(
        [
            "",
            "Module-level source reports are listed in `evidence.json`. This summary reports measured counters only; it does not infer that a backend threshold ran or passed.",
            "",
        ]
    )
    return "\n".join(lines)


def main() -> int:
    args = parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)
    junit_files: list[Path] = []
    for folder in args.junit_dir:
        if folder.is_file() and folder.suffix.lower() == ".xml":
            junit_files.append(folder.resolve())
        elif folder.is_dir():
            junit_files.extend(path.resolve() for path in folder.rglob("*.xml"))
    junit_files = sorted(set(junit_files))
    jacoco_files: list[Path] = []
    for folder in args.jacoco_dir:
        if folder.is_file() and folder.name == "jacoco.xml":
            jacoco_files.append(folder.resolve())
        elif folder.is_dir():
            jacoco_files.extend(path.resolve() for path in folder.rglob("jacoco.xml"))
    jacoco_files = sorted(set(jacoco_files))

    try:
        metadata = read_metadata(args.metadata)
    except ValueError as error:
        print(f"Evidence generation failed: {error}", file=sys.stderr)
        return 2

    outcomes, junit_errors = junit_outcomes(junit_files)
    coverage, jacoco_errors = jacoco_reports(jacoco_files)
    documented = documented_cases(args.case_docs_dir)
    rows = build_register(metadata, documented, outcomes)
    write_register(args.output_dir / "test-register.csv", rows)
    (args.output_dir / "test-register.md").write_text(markdown_register(rows), encoding="utf-8")

    manifest = {
        "generated_at_utc": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "run_id": os_value("GITHUB_RUN_ID", "local"),
        "test_commit": os_value("GITHUB_SHA", "unknown"),
        "backend_commit": os_value("BACKEND_COMMIT_SHA", "unknown"),
        "backend_run_id": os_value("BACKEND_RUN_ID", "not-supplied"),
        "junit_reports": [path.as_posix() for path in junit_files],
        "junit_test_method_count": len(outcomes),
        "mapped_case_count": len(metadata),
        "documented_case_count": len(documented),
        "unmapped_executed_count": sum(1 for row in rows if row["mapping_status"] == "Unmapped"),
        "coverage_status": "Measured" if coverage else "Unavailable",
        "jacoco_reports": coverage,
        "errors": junit_errors + jacoco_errors,
    }
    (args.output_dir / "evidence.json").write_text(
        json.dumps(manifest, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )
    (args.output_dir / "coverage-summary.md").write_text(markdown_coverage(coverage), encoding="utf-8")
    if not junit_files:
        print("Warning: no JUnit XML reports were supplied; mapped cases are marked Not Run.", file=sys.stderr)
    if manifest["unmapped_executed_count"]:
        print(
            f"Warning: {manifest['unmapped_executed_count']} executed test methods need explicit case/requirement mappings.",
            file=sys.stderr,
        )
    for error in manifest["errors"]:
        print(f"Warning: {error}", file=sys.stderr)
    print(
        f"Evidence generated: {len(rows)} register rows, {len(coverage)} JaCoCo reports ({manifest['coverage_status']})."
    )
    return 1 if junit_errors or jacoco_errors else 0


if __name__ == "__main__":
    raise SystemExit(main())

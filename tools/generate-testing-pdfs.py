#!/usr/bin/env python3
"""Render the guideline's test-register, coverage, and performance PDFs."""

from __future__ import annotations

import argparse
import csv
import json
import re
import textwrap
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path

import fitz

PAGE_W, PAGE_H = 842, 595
MARGIN = 28
NAVY = (0.08, 0.17, 0.29)
BLUE = (0.12, 0.38, 0.62)
INK = (0.15, 0.18, 0.22)
MUTED = (0.38, 0.42, 0.47)
RED = (0.65, 0.16, 0.16)
GREEN = (0.12, 0.43, 0.27)


def ascii_text(value: object) -> str:
    return str(value or "").encode("ascii", "replace").decode("ascii")


class Report:
    def __init__(self, title: str, subtitle: str):
        self.doc = fitz.open()
        self.title, self.subtitle = title, subtitle
        self.page_number = 0
        self.new_page()

    def new_page(self):
        self.page = self.doc.new_page(width=PAGE_W, height=PAGE_H)
        self.page_number += 1
        self.page.draw_rect(fitz.Rect(0, 0, PAGE_W, 62), color=NAVY, fill=NAVY)
        self.page.insert_text((MARGIN, 29), ascii_text(self.title), fontsize=17, fontname="hebo", color=(1, 1, 1))
        self.page.insert_text((MARGIN, 49), ascii_text(self.subtitle), fontsize=8.5, fontname="helv", color=(0.86, 0.9, 0.95))
        self.page.draw_line((MARGIN, PAGE_H - 25), (PAGE_W - MARGIN, PAGE_H - 25), color=(0.82, 0.85, 0.88), width=0.5)
        self.page.insert_text((MARGIN, PAGE_H - 12), "Mentify testing evidence | Generated " + datetime.now(timezone.utc).strftime("%Y-%m-%d UTC"), fontsize=7.5, fontname="helv", color=MUTED)
        self.page.insert_text((PAGE_W - 65, PAGE_H - 12), f"Page {self.page_number}", fontsize=7.5, fontname="helv", color=MUTED)
        self.y = 78

    def ensure(self, height: float):
        if self.y + height > PAGE_H - 38:
            self.new_page()

    def heading(self, title: str):
        self.ensure(26)
        self.page.insert_text((MARGIN, self.y + 13), ascii_text(title), fontsize=12, fontname="hebo", color=BLUE)
        self.y += 21

    def paragraph(self, value: str, size: float = 9, color=INK, width: float = PAGE_W - 2 * MARGIN):
        lines = []
        for raw in ascii_text(value).splitlines() or [""]:
            lines.extend(textwrap.wrap(raw, width=max(25, int(width / (size * 0.53))), break_long_words=True) or [""])
        self.ensure(len(lines) * (size + 3) + 5)
        for line in lines:
            self.page.insert_text((MARGIN, self.y + size), line, fontsize=size, fontname="helv", color=color)
            self.y += size + 3
        self.y += 3

    def save(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        self.doc.save(path, garbage=4, deflate=True)
        self.doc.close()


def read_rows(path: Path) -> list[dict[str, str]]:
    with path.open(newline="", encoding="utf-8-sig") as stream:
        return list(csv.DictReader(stream))


def add_table(report: Report, rows: list[dict[str, str]], columns: list[tuple[str, str, float]], fontsize=7.1):
    def draw_header():
        report.ensure(25)
        x, y = MARGIN, report.y
        for label, _, width in columns:
            report.page.draw_rect(fitz.Rect(x, y, x + width, y + 21), color=BLUE, fill=BLUE, width=0.4)
            report.page.insert_text((x + 3, y + 14), ascii_text(label), fontsize=fontsize, fontname="hebo", color=(1, 1, 1))
            x += width
        report.y += 21

    draw_header()
    for row_index, row in enumerate(rows):
        cell_lines, max_lines = [], 1
        for _, key, width in columns:
            chars = max(7, int((width - 7) / (fontsize * 0.51)))
            lines = textwrap.wrap(ascii_text(row.get(key, "")), width=chars, break_long_words=True) or [""]
            cell_lines.append(lines)
            max_lines = max(max_lines, len(lines))
        height = max(19, max_lines * (fontsize + 2.1) + 6)
        if report.y + height > PAGE_H - 38:
            report.new_page()
            draw_header()
        x, top = MARGIN, report.y
        for idx, ((_, _, width), lines) in enumerate(zip(columns, cell_lines)):
            fill = (0.965, 0.974, 0.985) if row_index % 2 == 0 else (1, 1, 1)
            report.page.draw_rect(fitz.Rect(x, top, x + width, top + height), color=(0.83, 0.86, 0.89), fill=fill, width=0.35)
            joined = " ".join(lines)
            color = GREEN if idx == len(columns) - 1 and "Passed" in joined else RED if idx == len(columns) - 1 and any(w in joined for w in ["Failed", "Blocked"]) else INK
            for n, line in enumerate(lines):
                report.page.insert_text((x + 3, top + fontsize + 3 + n * (fontsize + 2.1)), line, fontsize=fontsize, fontname="helv", color=color)
            x += width
        report.y += height


def register_pdf(register_path: Path, evidence_path: Path, out: Path, test_sha: str, branch: str):
    rows = read_rows(register_path)
    evidence = json.loads(evidence_path.read_text(encoding="utf-8")) if evidence_path.exists() else {}
    counts = Counter(row.get("status", "Not Run") for row in rows)
    report = Report("Test Case Register", "Requirement-linked cases and recorded execution status")
    report.heading("Execution snapshot")
    report.paragraph(f"Test branch: {branch}. PDF generation source SHA: {test_sha or 'not supplied'}.")
    report.paragraph(f"Register rows: {len(rows)} | Passed: {counts.get('Passed', 0)} | Failed: {counts.get('Failed', 0)} | Skipped: {counts.get('Skipped', 0)} | Not Run: {counts.get('Not Run', 0)} | Not Mapped: {counts.get('Not Mapped', 0)}.")
    report.paragraph(f"Evidence metadata: run={evidence.get('run_id', 'not recorded')}; test SHA={evidence.get('test_commit', 'not recorded')}; backend run={evidence.get('backend_run_id', 'not supplied')}; backend SHA={evidence.get('backend_commit', 'not supplied')}. Historical local JUnit results without a captured source SHA are not attributed to the current commit.")
    report.paragraph("Actual output is represented by the recorded execution status and JUnit report reference. Not Run rows are not treated as passes. Detailed case source, preconditions, and expected behavior remain in the Markdown catalogs.", color=MUTED)
    report.heading("Case-by-case register")
    normalized = []
    for row in rows:
        status = row.get("status", "Not Run")
        actual = {"Passed": "Passed; see JUnit report", "Failed": "Failed; see JUnit report", "Skipped": "Skipped: " + row.get("skip_reason", "see JUnit report")}.get(status, "Not executed in this run")
        normalized.append({
            **row,
            "case_input": row.get("scenario", ""),
            "test_file": f"{row.get('test_class', '')}#{row.get('test_method', '')}",
            "type_priority": f"{row.get('level', '')} / {row.get('priority', '')}",
            "preconditions": row.get("required_environment", ""),
            "expected_actual_status": f"Expected: {row.get('expected_result', '')} | Actual: {actual} | Status: {status}",
        })
    columns = [
        ("Case ID", "test_case_id", 68),
        ("Requirement", "requirement", 115),
        ("Case / Input", "case_input", 130),
        ("Test file / method", "test_file", 180),
        ("Type / priority", "type_priority", 70),
        ("Preconditions", "preconditions", 100),
        ("Expected / actual / status", "expected_actual_status", 123),
    ]
    add_table(report, normalized, columns, fontsize=6.8)
    report.save(out)


def coverage_pdf(summary_path: Path, out: Path, test_sha: str, branch: str, backend_run_id: str, backend_sha: str, backend_conclusion: str):
    text = summary_path.read_text(encoding="utf-8") if summary_path.exists() else ""
    report = Report("Backend Coverage Report", "Imported JaCoCo measurements; backend CI is the threshold authority")
    report.heading("Provenance")
    report.paragraph(f"Test branch: {branch}; test repository SHA: {test_sha or 'not supplied'}.")
    if backend_run_id and backend_sha and backend_conclusion:
        report.paragraph(f"TEST-042 backend CI run: {backend_run_id}; backend source SHA: {backend_sha}; conclusion: {backend_conclusion}.")
    else:
        report.paragraph("No TEST-042 backend CI run identity was supplied to this PDF generation.", color=RED)
    found = re.search(r"Backend workflow run: ([^;]+); backend source SHA: ([^.]+)", text)
    if found:
        report.paragraph(f"Backend workflow run: {found.group(1).strip(chr(96))}; backend source SHA: {found.group(2).strip().strip(chr(96))}.")
    else:
        report.paragraph("Backend run ID and source SHA were not present in the supplied coverage summary.", color=RED)
    report.heading("Measured coverage")
    table_rows = []
    for line in text.splitlines():
        if not line.startswith("|") or "Category" in line:
            continue
        cells = [part.strip().strip(chr(96)) for part in line.strip("|").split("|")]
        if len(cells) == 5 and cells[0] and not set(cells[0]) <= {"-", ":"}:
            table_rows.append({"category": cells[0], "modules": cells[1], "line": cells[2], "branch": cells[3], "instruction": cells[4]})
    if table_rows:
        add_table(report, table_rows, [("Category", "category", 260), ("Modules", "modules", 80), ("Line", "line", 105), ("Branch", "branch", 105), ("Instruction", "instruction", 145)], fontsize=8)
    else:
        report.paragraph("No JaCoCo XML was imported into this test-repository run. The latest separately recorded backend CI aggregate is run 40172276922 at backend SHA 40deaa168180365c3f9370aefcffe92471c58c26: 12 unit modules, 53.28% line, 33.73% branch, and 50.55% instruction coverage.", color=RED)
    report.heading("Guideline comparison")
    report.paragraph("The guideline requires at least 80% line and branch coverage for new code, 100% API endpoint integration-test coverage, and 90% branch coverage for critical business logic (SENG 34213 System Development Project Guideline, section 6.4).")
    if backend_run_id and backend_sha and backend_conclusion == "success":
        report.paragraph("The current TEST-042 backend unit/coverage workflow completed successfully, including the assignment-service unit job and JaCoCo artifact. Its aggregate values, when shown above, remain a separate measurement and are not changed-code coverage. The per-class 90% branch gate covers QuizGradingServiceImpl and GradingServiceImpl, mapped to SDS sections 4.5 and 4.6. Local full assignment-service verify also passed: 95 tests, zero failures/errors/skips; Quiz reached 35/35 lines and 14/14 branches, and Assignment reached 68/68 lines and 38/38 branches.", color=GREEN)
    else:
        report.paragraph("Aggregate figures are not changed-code coverage. Backend TEST-042 adds a per-class 90% branch gate for QuizGradingServiceImpl and GradingServiceImpl, mapped to SDS sections 4.5 and 4.6. Full local assignment-service verify passed: 95 tests, zero failures/errors/skips; both classes reached 100% line and branch coverage. No successful TEST-042 backend CI run was supplied to this PDF generation.", color=RED)
    report.paragraph("No integration JaCoCo report is attached to this evidence. The route matrix links source-level requests/assertions; it does not establish that every effective API endpoint passed an integration test. The 80% changed-code line/branch requirement also still needs CI evidence.", color=MUTED)
    report.save(out)


def performance_pdf(out: Path, test_sha: str, branch: str):
    report = Report("Performance Test Report", "Current execution status and required follow-up")
    report.heading("Execution status")
    report.paragraph(f"Test branch: {branch}; test repository SHA: {test_sha or 'not supplied'}.")
    report.paragraph("No approved dedicated performance target, scoped synthetic account, or completed performance workflow run was available when this report was generated. There are no measured latency, throughput, error-rate, timeout, or request-count values to report.", color=RED)
    report.paragraph("Status: BLOCKED / NOT RUN. This is an execution-status report; it is not a performance pass and must not be submitted as measured performance results.")
    report.heading("Evidence required to complete the run")
    for item in [
        "Configure the protected dedicated-performance GitHub Environment and its required reviewer.",
        "Set PERFORMANCE_BASE_URL and PERFORMANCE_ALLOWED_HOST_SUFFIX to the approved non-production Gateway.",
        "Store STUDENT_TEST_USERNAME and STUDENT_TEST_PASSWORD as environment secrets; never include credentials in the report.",
        "Set optional PERFORMANCE_COURSE_ID only when the test needs a safe course fixture.",
        "Dispatch the bounded workflow with its confirmation phrase and deployed backend SHA.",
        "Retain the successful workflow URL and sanitized artifact with request count, latency percentiles, throughput, error rate, and timeout count.",
        "Run at least twice before selecting a baseline; record each run's duration and workload.",
    ]:
        report.paragraph("- " + item)
    report.heading("Interpretation")
    report.paragraph("The suite is a bounded performance baseline, not a load/stress or capacity certification. Compare measured results with project targets only after a successful run against the approved dedicated environment.")
    report.save(out)


def summary_pdf(register_path: Path, route_matrix: Path, coverage_summary: Path, out: Path, test_sha: str, branch: str, backend_run_id: str, backend_sha: str, backend_conclusion: str, test_ci_run_id: str = "", test_ci_sha: str = "", test_ci_conclusion: str = ""):
    cases = read_rows(register_path)
    counts = Counter(row.get("status", "Not Run") for row in cases)
    routes = read_rows(route_matrix) if route_matrix.exists() else []
    route_counts = Counter(row.get("coverage_status", "") for row in routes)
    report = Report("Testing Evidence Summary", "Final report testing chapter - verified outcomes and remaining execution evidence")
    report.heading("Scope and provenance")
    report.paragraph("This summary follows SENG 34213 System Development Project Guideline sections 6.2, 6.4, 10.1, and 10.4. It distinguishes local test results, CI evidence, source-level endpoint links, and live environment results.")
    report.paragraph(f"Test repository branch: {branch}; summary source SHA: {test_sha}. The repository register contains {len(cases)} requirement-linked case rows. The route matrix contains {len(routes)} effective HTTP controller route rows.")
    if test_ci_run_id and test_ci_sha and test_ci_conclusion:
        report.paragraph(f"Test repository CI: run {test_ci_run_id}; source SHA {test_ci_sha}; conclusion {test_ci_conclusion}.", color=GREEN if test_ci_conclusion == "success" else RED)
    report.heading("Test execution results")
    report.paragraph("The test repository Maven clean verify completed successfully: 103 JUnit methods passed. The evidence register maps those results to 117 Passed case rows; 546 rows are Not Run, with zero Failed, Skipped, or Unmapped rows. Multiple case rows may map to one JUnit method.", color=GREEN)
    report.paragraph("The 235-row route matrix records source-linked request/assertion categories: 162 success and negative assertions, 46 success assertions, and 27 negative assertions only. These are implementation links; they do not prove live integration execution for every route.")
    report.paragraph("Historical live evidence is incomplete: the documented run had 17 methods pass and 23 mapped rows pass, while 640 rows were not run. That run did not retain a test SHA or GitHub run ID, so it is not used as current release evidence.", color=RED)
    report.paragraph("Frontend: 5 Student/Admin Playwright E2E tests passed locally. The AI chat E2E was excluded under the team's decision to verify AI manually. The tested E2E files match the frontend development branch; no frontend code change was needed.")
    report.heading("Coverage results and guideline interpretation")
    report.paragraph("The guideline's 80% line and branch target applies to new or modified production code. Backend TEST-042 added tests, JaCoCo configuration, CI, and documentation; it did not change production Java source. Therefore the whole-backend aggregate is not the 80% acceptance gate for this branch.")
    report.paragraph("Latest available backend-wide aggregate baseline: run 40172276922, SHA 40deaa168180365c3f9370aefcffe92471c58c26. It measured 12 unit modules at 53.28% line, 33.73% branch, and 50.55% instruction. This is historical context only, not a current-run aggregate or the changed-production-code result for TEST-042.")
    if backend_run_id and backend_sha and backend_conclusion:
        report.paragraph(f"Current backend unit/coverage CI: run {backend_run_id}; source SHA {backend_sha}; conclusion {backend_conclusion}. All 14 unit/coverage matrix jobs passed and 12 JaCoCo module artifacts were published, including assignment-service.", color=GREEN if backend_conclusion == "success" else RED)
    else:
        report.paragraph("The current backend unit/coverage CI run ID, SHA, and conclusion were not supplied to this summary.", color=RED)
    report.paragraph("SDS critical logic: the Maven build enforces at least 90% branch coverage independently for QuizGradingServiceImpl (SDS quiz auto-grading, section 4.5) and GradingServiceImpl (SDS assignment grading, section 4.6). Both measured 100% line and branch coverage locally: Quiz 35/35 lines and 14/14 branches; Assignment 68/68 lines and 38/38 branches. The full assignment-service verify passed 95 tests, and its backend CI job passed with a JaCoCo artifact.", color=GREEN)
    report.new_page()
    report.heading("Guideline acceptance matrix")
    remaining = [
        {"criterion": "New/changed production code", "target": ">=80% line and branch", "evidence": "TEST-042 changes no backend production Java source; this branch does not claim a whole-backend 80% aggregate.", "status": "Target scope: not triggered by this branch."},
        {"criterion": "API integration", "target": "At least one passing integration case per effective endpoint", "evidence": "235 route rows have source links; 546 register rows remain Not Run; no current live endpoint run is recorded.", "status": "Incomplete; run the PR-triggered backend integration workflow."},
        {"criterion": "SDS critical grading", "target": ">=90% branch per scoped class", "evidence": "QuizGradingServiceImpl: 100% (14/14); GradingServiceImpl: 100% (38/38); backend CI verify passed.", "status": "Met for the two documented grading classes."},
        {"criterion": "Performance", "target": "Measured bounded run and reported latency/throughput/errors", "evidence": "No performance measurements or successful workflow run are recorded.", "status": "Pending dedicated-performance run."},
        {"criterion": "Staging smoke", "target": "Approved read-only staging run with run ID and deployed SHA", "evidence": "No staging run ID, deployment SHA, or smoke artifact is recorded.", "status": "Pending staging run."},
        {"criterion": "AI verification", "target": "Team's chosen manual verification", "evidence": "AI chat E2E was excluded; the team reports manual verification.", "status": "No automated/live AI result is claimed."},
    ]
    add_table(report, remaining, [("Criterion", "criterion", 105), ("Guideline target", "target", 155), ("Evidence", "evidence", 300), ("Assessment", "status", 226)], fontsize=6.7)
    report.paragraph("Not Run/Pending is a recorded execution state, not a failed test result. Update the relevant report only after a successful run produces its JUnit/JaCoCo or sanitized environment artifact.", color=MUTED)
    report.save(out)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--register", type=Path, default=Path("target/test-evidence/test-register.csv"))
    parser.add_argument("--evidence", type=Path, default=Path("target/test-evidence/evidence.json"))
    parser.add_argument("--coverage-summary", type=Path, default=Path("target/test-evidence/coverage-summary.md"))
    parser.add_argument("--route-matrix", type=Path, default=Path("docs/backend-route-test-matrix.csv"))
    parser.add_argument("--output-dir", type=Path, default=Path("docs/testing"))
    parser.add_argument("--test-sha", default="unknown")
    parser.add_argument("--branch", default="unknown")
    parser.add_argument("--backend-ci-run-id", default="")
    parser.add_argument("--backend-ci-sha", default="")
    parser.add_argument("--backend-ci-conclusion", default="")
    parser.add_argument("--test-ci-run-id", default="")
    parser.add_argument("--test-ci-sha", default="")
    parser.add_argument("--test-ci-conclusion", default="")
    args = parser.parse_args()
    if not args.register.exists():
        parser.error(f"test register does not exist: {args.register}")
    register_pdf(args.register, args.evidence, args.output_dir / "test-register.pdf", args.test_sha, args.branch)
    coverage_pdf(args.coverage_summary, args.output_dir / "coverage-sprint8.pdf", args.test_sha, args.branch, args.backend_ci_run_id, args.backend_ci_sha, args.backend_ci_conclusion)
    performance_pdf(args.output_dir / "performance-report.pdf", args.test_sha, args.branch)
    summary_pdf(args.register, args.route_matrix, args.coverage_summary, args.output_dir / "testing-summary.pdf", args.test_sha, args.branch, args.backend_ci_run_id, args.backend_ci_sha, args.backend_ci_conclusion, args.test_ci_run_id, args.test_ci_sha, args.test_ci_conclusion)
    print(f"Generated PDFs under {args.output_dir}")


if __name__ == "__main__":
    main()

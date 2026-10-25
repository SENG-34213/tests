#!/usr/bin/env python3
"""Write a sanitized performance summary from Surefire XML output."""

from __future__ import annotations

import argparse
import os
import re
import sys
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path


NUMBER = r"(?:\d+(?:\.\d+)?|\.\d+)"
METRIC = re.compile(
    rf"PerformanceMetrics\{{scenario='(?P<scenario>[A-Za-z0-9_.-]+)', "
    rf"requests=(?P<requests>\d+), successes=(?P<successes>\d+), "
    rf"failures=(?P<failures>\d+), timeouts=(?P<timeouts>\d+), "
    rf"errorRatePercent=(?P<error_rate>{NUMBER}), minMillis=(?P<min>\d+), "
    rf"maxMillis=(?P<max>\d+), averageMillis=(?P<average>{NUMBER}), "
    rf"p50Millis=(?P<p50>\d+), p90Millis=(?P<p90>\d+), "
    rf"p95Millis=(?P<p95>\d+), p99Millis=(?P<p99>\d+), "
    rf"throughputPerSecond=(?P<throughput>{NUMBER})\}}"
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--junit-dir", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    return parser.parse_args()


def collect_metrics(report_dir: Path) -> list[dict[str, str]]:
    metrics: list[dict[str, str]] = []
    for report in sorted(report_dir.glob("TEST-*.xml")):
        root = ET.parse(report).getroot()
        for output in root.findall(".//system-out"):
            for match in METRIC.finditer(output.text or ""):
                metrics.append(match.groupdict())
    return metrics


def render(metrics: list[dict[str, str]]) -> str:
    lines = [
        "# Dedicated Performance Baseline Evidence",
        "",
        f"- Generated (UTC): {datetime.now(timezone.utc).isoformat(timespec='seconds')}",
        f"- Workflow run: {os.environ.get('GITHUB_RUN_ID', 'local')}",
        f"- Test repository commit: {os.environ.get('GITHUB_SHA', 'unknown')}",
        f"- Backend deployment revision: {os.environ.get('PERFORMANCE_DEPLOYMENT_REVISION', 'unknown')}",
        "",
    ]
    if not metrics:
        lines.extend(["No performance metrics were captured in the JUnit reports.", ""])
        return "\n".join(lines)
    lines.extend([
        "| Scenario | Requests | Successes | Failures | Timeouts | Error % | p50 ms | p95 ms | p99 ms | Throughput/s |",
        "|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|",
    ])
    for row in metrics:
        lines.append(
            f"| {row['scenario']} | {row['requests']} | {row['successes']} | {row['failures']} "
            f"| {row['timeouts']} | {row['error_rate']} | {row['p50']} | {row['p95']} "
            f"| {row['p99']} | {row['throughput']} |"
        )
    lines.append("")
    return "\n".join(lines)


def main() -> int:
    args = parse_args()
    try:
        metrics = collect_metrics(args.junit_dir)
    except (OSError, ET.ParseError) as error:
        print(f"Could not read performance JUnit evidence: {error}", file=sys.stderr)
        return 2
    args.output.parent.mkdir(parents=True, exist_ok=True)
    summary = render(metrics)
    args.output.write_text(summary, encoding="utf-8")
    print(summary)
    if not metrics:
        return 1
    names = [row["scenario"] for row in metrics]
    if len(names) != len(set(names)):
        print("Duplicate performance scenario metrics were found.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

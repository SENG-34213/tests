"""Tests for producing secret-safe performance evidence."""

import importlib.util
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch


SCRIPT = Path(__file__).with_name("performance-evidence.py")
SPEC = importlib.util.spec_from_file_location("performance_evidence", SCRIPT)
assert SPEC is not None and SPEC.loader is not None
performance_evidence = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(performance_evidence)


class PerformanceEvidenceTests(unittest.TestCase):
    def test_extracts_metrics_and_writes_only_safe_summary_fields(self):
        metric = (
            "PerformanceMetrics{scenario='gateway-read', requests=30, successes=30, "
            "failures=0, timeouts=0, errorRatePercent=0.0, minMillis=12, maxMillis=80, "
            "averageMillis=24.5, p50Millis=20, p90Millis=41, p95Millis=55, p99Millis=78, "
            "throughputPerSecond=10.2}"
        )
        with tempfile.TemporaryDirectory() as directory:
            report_dir = Path(directory)
            (report_dir / "TEST-Performance.xml").write_text(
                f"<testsuite><testcase><system-out><![CDATA[{metric}]]></system-out>"
                "</testcase></testsuite>",
                encoding="utf-8",
            )

            rows = performance_evidence.collect_metrics(report_dir)
            with patch.dict("os.environ", {
                "GITHUB_RUN_ID": "1234",
                "GITHUB_SHA": "test-sha",
                "PERFORMANCE_DEPLOYMENT_REVISION": "backend-sha",
            }):
                summary = performance_evidence.render(rows)

        self.assertEqual("gateway-read", rows[0]["scenario"])
        self.assertIn("| gateway-read | 30 | 30 | 0 | 0 | 0.0 | 20 | 55 | 78 | 10.2 |", summary)
        self.assertIn("backend-sha", summary)
        self.assertNotIn("PERFORMANCE_BASE_URL", summary)

    def test_no_metrics_is_explicitly_reported(self):
        self.assertIn(
            "No performance metrics were captured",
            performance_evidence.render([]),
        )


if __name__ == "__main__":
    unittest.main()

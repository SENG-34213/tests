import csv
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

import fitz

_module_spec = importlib.util.spec_from_file_location(
    "generate_testing_pdfs", Path(__file__).with_name("generate-testing-pdfs.py")
)
pdfs = importlib.util.module_from_spec(_module_spec)
_module_spec.loader.exec_module(pdfs)


class TestingPdfGenerationTests(unittest.TestCase):
    def test_generates_register_coverage_and_performance_pdfs(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            register = root / "register.csv"
            evidence = root / "evidence.json"
            summary = root / "coverage.md"
            routes = root / "routes.csv"
            output = root / "out"
            fields = [
                "test_case_id", "requirement", "service", "scenario", "priority",
                "test_class", "test_method", "level", "required_environment",
                "expected_result", "status", "skip_reason", "report_file",
                "run_id", "run_date_utc", "test_commit", "backend_commit",
                "mapping_status",
            ]
            with register.open("w", newline="", encoding="utf-8") as stream:
                writer = csv.DictWriter(stream, fieldnames=fields)
                writer.writeheader()
                writer.writerow({
                    "test_case_id": "TC-COURSE-001",
                    "requirement": "Course readiness",
                    "service": "course",
                    "scenario": "Gateway readiness request",
                    "priority": "high",
                    "test_class": "CourseReadinessIT",
                    "test_method": "gatewayIsReady",
                    "level": "integration",
                    "required_environment": "Gateway",
                    "expected_result": "HTTP 200",
                    "status": "Passed",
                    "report_file": "target/failsafe-reports/report.xml",
                })
            evidence.write_text(json.dumps({
                "run_id": "40172276922",
                "test_commit": "a" * 40,
                "backend_run_id": "40172276922",
                "backend_commit": "b" * 40,
            }), encoding="utf-8")
            tick = chr(96)
            summary.write_text(
                "# Backend Coverage Evidence\n\n"
                "Backend workflow run: " + tick + "40172276922" + tick
                + "; backend source SHA: " + tick + "b" * 40 + tick + ".\n\n"
                + "| Category | Module count | Line coverage | Branch coverage | Instruction coverage |\n"
                + "|---|---:|---:|---:|---:|\n"
                + "| unit aggregate | 12 | 53.28% | 33.73% | 50.55% |\n",
                encoding="utf-8",
            )
            with routes.open("w", newline="", encoding="utf-8") as stream:
                writer = csv.DictWriter(stream, fieldnames=["coverage_status"])
                writer.writeheader()
                writer.writerow({"coverage_status": "REQUEST_WITH_SUCCESS_RESPONSE_ASSERTION_LINKED"})

            pdfs.register_pdf(register, evidence, output / "test-register.pdf", "a" * 40, "feature/test")
            pdfs.coverage_pdf(
                summary, output / "coverage-sprint8.pdf", "a" * 40, "feature/test",
                "40180383416", "8cb633c2c12868cb3239a8940e24cad1abd42746", "success",
            )
            pdfs.performance_pdf(output / "performance-report.pdf", "a" * 40, "feature/test")
            pdfs.summary_pdf(
                register, routes, summary, output / "testing-summary.pdf", "a" * 40, "feature/test",
                "40180383416", "8cb633c2c12868cb3239a8940e24cad1abd42746", "success",
                "40181135024", "2f2e76a9f2abbcda0922a09e29deacf1c1462cac", "success",
            )

            expected = {
                "test-register.pdf": ["TC-COURSE-001", "Passed", "Course readiness"],
                "coverage-sprint8.pdf": ["53.28%", "33.73%", "40180383416", "conclusion: success"],
                "performance-report.pdf": ["BLOCKED / NOT RUN", "PERFORMANCE_BASE_URL"],
                "testing-summary.pdf": [
                    "235-row route matrix", "546 rows", "Not Run, with zero Failed",
                    "80% line and branch target",
                    "14 unit/coverage matrix jobs passed", "90% branch coverage independently",
                    "40181135024", "Guideline acceptance matrix", "Not Run/Pending",
                    "historical context only", "not a current-run aggregate",
                ],
            }
            for name, markers in expected.items():
                path = output / name
                self.assertTrue(path.is_file())
                self.assertGreater(path.stat().st_size, 500)
                with fitz.open(path) as document:
                    text = "\n".join(page.get_text() for page in document)
                for marker in markers:
                    self.assertIn(marker, text)


if __name__ == "__main__":
    unittest.main()

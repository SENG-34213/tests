"""Regression tests for joining short metadata class names to JUnit reports."""

import importlib.util
import unittest
from pathlib import Path


MODULE_PATH = Path(__file__).with_name("test-evidence.py")
SPEC = importlib.util.spec_from_file_location("test_evidence", MODULE_PATH)
assert SPEC is not None and SPEC.loader is not None
test_evidence = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(test_evidence)


class ResolveOutcomeKeyTest(unittest.TestCase):
    def test_resolves_simple_class_name_to_unique_junit_class(self):
        outcomes = {"com.mentify.tests.course.CourseClientTest#validatesIds": {"status": "Passed"}}

        resolved = test_evidence.resolve_outcome_key("CourseClientTest", "validatesIds", outcomes)

        self.assertEqual("com.mentify.tests.course.CourseClientTest#validatesIds", resolved)

    def test_preserves_exact_fully_qualified_class_name(self):
        key = "com.mentify.tests.course.CourseClientTest#validatesIds"
        outcomes = {key: {"status": "Passed"}}

        self.assertEqual(key, test_evidence.resolve_outcome_key(
            "com.mentify.tests.course.CourseClientTest", "validatesIds", outcomes))

    def test_does_not_guess_when_simple_class_name_is_ambiguous(self):
        outcomes = {
            "com.mentify.tests.course.CourseClientTest#validatesIds": {"status": "Passed"},
            "com.mentify.tests.other.CourseClientTest#validatesIds": {"status": "Passed"},
        }

        self.assertEqual("CourseClientTest#validatesIds",
                         test_evidence.resolve_outcome_key("CourseClientTest", "validatesIds", outcomes))


if __name__ == "__main__":
    unittest.main()

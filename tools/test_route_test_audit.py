import importlib.util
from pathlib import Path
import unittest


SCRIPT = Path(__file__).with_name("route-test-audit.py")
SPEC = importlib.util.spec_from_file_location("route_test_audit", SCRIPT)
route_test_audit = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(route_test_audit)


class JUnitTestBodyDiscoveryTests(unittest.TestCase):
    def test_expands_helper_request_and_assertion_under_annotated_test(self):
        source = """
        class ExampleIT {
            @Test
            void mappedCase() { verifyRequest(); }

            private void verifyRequest() {
                Response response = client.get("/route");
                assertEquals(200, response.statusCode());
            }

            private void unrelatedHelper() { client.delete("/other"); }
        }
        """

        tests = route_test_audit.junit_test_bodies(source)

        self.assertEqual(["mappedCase"], [name for name, _ in tests])
        self.assertIn('client.get("/route")', tests[0][1])
        self.assertIn("assertEquals(200", tests[0][1])
        self.assertNotIn('client.delete("/other")', tests[0][1])

    def test_does_not_report_private_helpers_as_tests(self):
        source = """
        class ExampleIT {
            @Test void firstCase() { helper(); }
            private void helper() { assertEquals(200, response.statusCode()); }
        }
        """

        tests = route_test_audit.junit_test_bodies(source)

        self.assertEqual(["firstCase"], [name for name, _ in tests])


if __name__ == "__main__":
    unittest.main()

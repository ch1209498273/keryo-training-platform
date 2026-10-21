"""Runner outcomes must follow discovered test results, including empty suites."""
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


RUNNER = Path(__file__).resolve().parents[1] / "scripts/baseline/run_python_tests.py"


class PythonTestEntrypointTest(unittest.TestCase):
    def run_suite(self, test_source=None):
        with tempfile.TemporaryDirectory(prefix="keryo python runner ") as directory:
            if test_source is not None:
                (Path(directory) / "test_example.py").write_text(test_source, encoding="utf-8")
            return subprocess.run(
                [sys.executable, "-B", str(RUNNER), directory],
                capture_output=True, text=True, errors="replace", timeout=30,
            )

    def test_empty_suite_cannot_pass(self):
        result = self.run_suite()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("No Python tests discovered", result.stdout + result.stderr)

    def test_passing_suite_reports_executed_count(self):
        result = self.run_suite("import unittest\nclass Example(unittest.TestCase):\n def test_ok(self): self.assertEqual(1 + 1, 2)\n")
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("PYTHON_TEST_RESULT tests=1 skipped=0", result.stdout)

    def test_failing_suite_returns_failure(self):
        result = self.run_suite("import unittest\nclass Example(unittest.TestCase):\n def test_bad(self): self.fail('fixture failure')\n")
        self.assertEqual(result.returncode, 1)
        self.assertIn("fixture failure", result.stderr)

    def test_skipped_suite_is_not_counted_as_pass(self):
        result = self.run_suite("import unittest\nclass Example(unittest.TestCase):\n @unittest.skip('fixture unavailable')\n def test_skip(self): pass\n")
        self.assertEqual(result.returncode, 1)
        self.assertIn("PYTHON_TEST_RESULT tests=1 skipped=1", result.stdout)


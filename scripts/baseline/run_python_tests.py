"""Run active database-free tests; empty/skipped suites are not a PASS."""
from pathlib import Path
import sys
import unittest


def main():
    source = Path(sys.argv[1]) if len(sys.argv) > 1 else Path(__file__).resolve().parents[2] / "tests"
    suite = unittest.defaultTestLoader.discover(str(source), pattern="test_*.py")
    if suite.countTestCases() == 0:
        print("No Python tests discovered; refusing an empty test run", file=sys.stderr)
        return 1
    result = unittest.TextTestRunner(verbosity=2).run(suite)
    print(f"PYTHON_TEST_RESULT tests={result.testsRun} skipped={len(result.skipped)}")
    return 0 if result.wasSuccessful() and not result.skipped else 1


if __name__ == "__main__":
    sys.exit(main())


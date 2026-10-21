"""Exercise the real PowerShell runner without a database or network.

The Java process is substituted only for runner exit-code/summary tests;
the actual project tests must still run separately on JDK 17.
"""

import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest
import zipfile


ROOT = Path(__file__).resolve().parents[1]
RUNNER = ROOT / "scripts/baseline/run_java_tests.ps1"
POWERSHELL = shutil.which("powershell.exe")


class JavaTestEntrypointTest(unittest.TestCase):
    def setUp(self):
        self.assertIsNotNone(POWERSHELL, "Windows PowerShell is required, not a skipped test")
        self.temporary = tempfile.TemporaryDirectory(prefix="keryo runner test ")
        self.addCleanup(self.temporary.cleanup)
        self.base = Path(self.temporary.name)
        self.source = self.base / "src"
        self.source.mkdir()

    def source_test(self, name):
        path = self.source / f"{name}.java"
        path.write_text(f"package sample;\npublic class {name} {{}}\n", encoding="utf-8")

    def run_runner(self, *args, env=None):
        return subprocess.run(
            [POWERSHELL, "-NoProfile", "-ExecutionPolicy", "Bypass", "-File",
             str(RUNNER), "-TestSourceRoot", str(self.source), *args],
            env=env, capture_output=True, text=True, errors="replace", timeout=30,
        )

    def test_new_test_class_is_discovered_without_editing_a_list(self):
        self.source_test("FirstTest")
        first = self.run_runner("-ListOnly")
        self.assertEqual(first.returncode, 0, first.stdout + first.stderr)
        self.assertEqual(first.stdout.splitlines(), ["sample.FirstTest"])
        self.source_test("AddedTest")
        second = self.run_runner("-ListOnly")
        self.assertEqual(second.returncode, 0, second.stdout + second.stderr)
        self.assertEqual(second.stdout.splitlines(), ["sample.AddedTest", "sample.FirstTest"])

    def test_empty_inventory_fails_instead_of_claiming_success(self):
        result = self.run_runner("-ListOnly")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("No Java test classes", result.stdout + result.stderr)

    def java_fixture(self, summary, code):
        self.source_test("ExampleTest")
        target = self.base / "target"
        compiled = target / "test-classes/sample/ExampleTest.class"
        compiled.parent.mkdir(parents=True)
        compiled.touch()
        with zipfile.ZipFile(target / "yf-bev2-api.jar", "w") as jar:
            jar.writestr("BOOT-INF/lib/fixture.jar", b"")
        repo = self.base / "m2"
        for name in (
            "junit/junit/4.13.2/junit-4.13.2.jar",
            "org/hamcrest/hamcrest/2.2/hamcrest-2.2.jar",
            "org/springframework/spring-test/6.1.2/spring-test-6.1.2.jar",
            "org/mockito/mockito-core/5.7.0/mockito-core-5.7.0.jar",
            "net/bytebuddy/byte-buddy/1.14.10/byte-buddy-1.14.10.jar",
            "net/bytebuddy/byte-buddy-agent/1.14.10/byte-buddy-agent-1.14.10.jar",
            "org/objenesis/objenesis/3.3/objenesis-3.3.jar",
        ):
            dependency = repo / name
            dependency.parent.mkdir(parents=True, exist_ok=True)
            dependency.touch()
        executable = self.base / "fixture-java.cmd"
        executable.write_text(
            f"@echo off\necho %*\necho {summary}\nexit /b {code}\n", encoding="ascii"
        )
        env = dict(os.environ, BASELINE_BACKEND_TARGET=str(target),
                   BASELINE_RUNTIME_DIR=str(self.base / "runtime"),
                   BASELINE_MAVEN_REPO=str(repo), BASELINE_JAVA=str(executable))
        return env, compiled

    def test_executes_discovered_class_and_reports_count(self):
        env, _ = self.java_fixture("OK (2 tests)", 0)
        result = self.run_runner(env=env)
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        self.assertIn("sample.ExampleTest", result.stdout)
        self.assertIn("JAVA_TEST_RESULT classes=1 tests=2", result.stdout)

    def test_zero_tests_is_a_failure_even_with_zero_process_exit(self):
        env, _ = self.java_fixture("OK (0 tests)", 0)
        result = self.run_runner(env=env)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("No positive JUnit test count", result.stdout + result.stderr)

    def test_preserves_java_failure_exit_code(self):
        env, _ = self.java_fixture("FAILURES!!!", 9)
        result = self.run_runner(env=env)
        self.assertEqual(result.returncode, 9, result.stdout + result.stderr)

    def test_missing_compiled_class_cannot_silently_use_old_suite(self):
        env, compiled = self.java_fixture("OK (2 tests)", 0)
        compiled.unlink()
        result = self.run_runner(env=env)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Test class not compiled", result.stdout + result.stderr)

    def test_changed_test_source_cannot_silently_run_stale_bytecode(self):
        env, compiled = self.java_fixture("OK (2 tests)", 0)
        os.utime(compiled, (1, 1))
        result = self.run_runner(env=env)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("Test class is older than its source", result.stdout + result.stderr)


if __name__ == "__main__":
    unittest.main()


import importlib.util
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MODULE_PATH = ROOT / "start_all.py"
SPEC = importlib.util.spec_from_file_location("keryo_start_all", MODULE_PATH)
assert SPEC and SPEC.loader
start_all = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(start_all)


class StartAllMySqlWorkdirTest(unittest.TestCase):
    def test_uses_data_directory_next_to_defaults_file(self):
        with tempfile.TemporaryDirectory() as temporary_directory:
            config_path = Path(temporary_directory) / "my.ini"
            data_path = Path(temporary_directory) / "data"
            config_path.touch()
            data_path.mkdir()

            self.assertEqual(start_all.mysql_working_directory(config_path), data_path)

    def test_frontend_startup_timeout_allows_cold_vite_start(self):
        self.assertGreaterEqual(start_all.FRONTEND_STARTUP_TIMEOUT, 180)


if __name__ == "__main__":
    unittest.main()


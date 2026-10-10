import json
import tempfile
import unittest
import zipfile
from pathlib import Path

from modrinth_publisher import metadata_for, find_jar, verify_jar


class PublisherTests(unittest.TestCase):
    def create_jar(self, folder, target, version="1.1.0-dev", mismatch=False):
        path = Path(folder) / "DanmakuChat.jar"
        with zipfile.ZipFile(path, "w") as z:
            z.writestr("fabric.mod.json", json.dumps({
                "id": "danmakuchat", "version": version,
                "depends": {"minecraft": "wrong" if mismatch else target}
            }))
            z.writestr("com/danmakuchat/DanmakuChat.class", b"example")
            if target.startswith("26."):
                z.writestr("com/danmakuchat/config/ModMenuIntegration.class", b"example")
            if target == "1.21.11":
                z.writestr("danmakuchat.mixins.json",
                           json.dumps({"client": ["ChatHudAccessor", "ChatHudMixin12111"]}))
        return path

    def test_valid_26_3_mod(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = self.create_jar(tmp, "26.3")
            self.assertEqual(len(verify_jar(path, "26.3", "1.1.0-dev")), 128)

    def test_mismatched_mc_version_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = self.create_jar(tmp, "26.2", mismatch=True)
            with self.assertRaisesRegex(ValueError, "Minecraft dependency"):
                verify_jar(path, "26.2", "1.1.0-dev")

    def test_12111_mixin(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = self.create_jar(tmp, "1.21.11")
            verify_jar(path, "1.21.11", "1.1.0-dev")

    def test_source_jar_excluded(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = self.create_jar(tmp, "1.21.8")
            (Path(tmp) / "DanmakuChat-sources.jar").write_bytes(b"sources")
            self.assertEqual(path, find_jar(tmp))

    def test_exact_jar_name_does_not_reject_dev_version(self):
        with tempfile.TemporaryDirectory() as tmp:
            jar = self.create_jar(tmp, "26.3")
            expected = Path(tmp) / "DanmakuChat-26.3-1.1.0-dev.jar"
            jar.rename(expected)
            (Path(tmp) / "DanmakuChat-26.3-1.1.0-dev-sources.jar").write_bytes(b"other")
            self.assertEqual(find_jar(tmp, "26.3", "1.1.0-dev"), expected)

    def test_ambiguous_jars_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            self.create_jar(tmp, "26.3")
            (Path(tmp) / "another.jar").write_bytes(b"other")
            with self.assertRaisesRegex(ValueError, "Expected one"):
                find_jar(tmp)

    def test_metadata(self):
        data = metadata_for("26.3", "1.1.0-dev")
        self.assertEqual(data["version_number"], "1.1.0-dev-mc26.3")
        self.assertEqual(data["project_id"], "TIlGeskT")
        self.assertEqual(data["game_versions"], ["26.3"])
        self.assertEqual(data["status"], "listed")
        self.assertEqual(data["version_type"], "beta")


if __name__ == "__main__":
    unittest.main()

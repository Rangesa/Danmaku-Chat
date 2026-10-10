#!/usr/bin/env python3
"""Modrinth uploader; dry-run unless --publish is explicitly supplied."""
import argparse
import hashlib
import json
import os
import re
import sys
import urllib.error
import urllib.request
import uuid
import zipfile
from pathlib import Path

PROJECT_ID = "TIlGeskT"
PROJECT_SOURCE = "https://github.com/Rangesa/Danmaku-Chat"
API = "https://api.modrinth.com/v2"
USER_AGENT = "Rangesa/Danmaku-Chat-CI/1.0 (https://github.com/Rangesa/Danmaku-Chat)"
TARGETS = ("1.21.8", "1.21.11", "26.2", "26.3")


def mod_version():
    text = Path("gradle.properties").read_text(encoding="utf-8")
    match = re.search(r"^mod_version\s*=\s*(\S+)\s*$", text, re.M)
    if not match:
        raise ValueError("mod_version is missing")
    return match.group(1)


def find_jar(directory, target=None, version=None):
    directory = Path(directory)
    if target and version:
        expected = directory / f"DanmakuChat-{target}-{version}.jar"
        if not expected.is_file():
            matches = [p.name for p in directory.glob("*.jar")]
            raise ValueError(f"Expected exact release JAR {expected}; found: {matches}")
        return expected
    jar_files = [
        p for p in directory.glob("*.jar")
        if not p.stem.endswith(("-sources", "-javadoc", "-plain", "-all"))
    ]
    if len(jar_files) != 1:
        raise ValueError(f"Expected one distributable JAR in {directory}, found {len(jar_files)}")
    return jar_files[0]


def verify_jar(path, target, version):
    if target not in TARGETS:
        raise ValueError("Unsupported target " + target)
    if not zipfile.is_zipfile(path):
        raise ValueError("Invalid JAR " + str(path))
    with zipfile.ZipFile(path) as archive:
        names = set(archive.namelist())
        if "fabric.mod.json" not in names or "com/danmakuchat/DanmakuChat.class" not in names:
            raise ValueError("Missing mod metadata or entrypoint")
        meta = json.loads(archive.read("fabric.mod.json"))
        if meta.get("id") != "danmakuchat":
            raise ValueError("Incorrect mod ID")
        if meta.get("version") != version:
            raise ValueError("Incorrect mod version: " + str(meta.get("version")))
        if meta.get("depends", {}).get("minecraft") != target:
            raise ValueError("Incorrect Minecraft dependency")
        if target == "1.21.11":
            mixins = json.loads(archive.read("danmakuchat.mixins.json"))
            if "ChatHudMixin12111" not in mixins.get("client", []):
                raise ValueError("1.21.11 ChatHud mixin missing")
        if target.startswith("26.") and "com/danmakuchat/config/ModMenuIntegration.class" not in names:
            raise ValueError("26.x Mod Menu class missing")
    return hashlib.sha512(Path(path).read_bytes()).hexdigest()


def metadata_for(target, version):
    return {
        "name": f"DanmakuChat {version} for Minecraft {target}",
        "version_number": f"{version}-mc{target}",
        "changelog": (
            "Version-specific Fabric build. Source and changelog: "
            f"{PROJECT_SOURCE}. Includes AI-assisted compatibility code; "
            "refer to project content disclosures. "
            f"GitHub commit: {os.getenv('GITHUB_SHA', 'local')}."
        ),
        "dependencies": [
            {"version_id": None, "project_id": "P7dR8mSH",
             "file_name": None, "dependency_type": "required"},
            {"version_id": None, "project_id": "mOgUt4GM",
             "file_name": None, "dependency_type": "optional"},
        ],
        "game_versions": [target],
        "version_type": "beta" if any(word in version.lower() for word in ("dev", "alpha", "beta")) else "release",
        "loaders": ["fabric"],
        "featured": False,
        "status": "listed",
        "environment": "client_only",
        "project_id": PROJECT_ID,
        "file_parts": ["file"],
        "primary_file": "file",
    }


def api_json(method, path, token, payload=None, content_type=None):
    headers = {"User-Agent": USER_AGENT, "Accept": "application/json"}
    if token:
        headers["Authorization"] = token
    if content_type:
        headers["Content-Type"] = content_type
    req = urllib.request.Request(API + path, data=payload, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=45) as response:
            return json.load(response)
    except urllib.error.HTTPError as exc:
        reason = exc.read(1000).decode("utf-8", "replace")
        raise RuntimeError(f"Modrinth {method} {path}: HTTP {exc.code}: {reason}") from None


def multipart(meta, jar):
    boundary = "DanmakuChat-" + uuid.uuid4().hex
    body = (
        f"--{boundary}\r\nContent-Disposition: form-data; name=\"data\"\r\n"
        "Content-Type: application/json\r\n\r\n"
    ).encode("utf-8") + json.dumps(meta, ensure_ascii=False).encode("utf-8") + b"\r\n"
    body += (
        f"--{boundary}\r\nContent-Disposition: form-data; name=\"file\"; "
        f"filename=\"{jar.name}\"\r\nContent-Type: application/java-archive\r\n\r\n"
    ).encode("utf-8") + jar.read_bytes() + b"\r\n"
    body += f"--{boundary}--\r\n".encode("ascii")
    return body, f"multipart/form-data; boundary={boundary}"


def publish_target(directory, target, version, upload=False):
    jar = find_jar(directory, target, version)
    digest = verify_jar(jar, target, version)
    meta = metadata_for(target, version)
    print(f"Prepared {target} {jar.name}: {digest[:20]}... ({meta['version_number']})")
    if not upload:
        print("Dry run: no API write")
        return
    token = os.getenv("MODRINTH_TOKEN", "")
    if not token:
        raise RuntimeError("MODRINTH_TOKEN secret is missing")
    if os.getenv("MODRINTH_PUBLISH_ENABLED") != "true":
        raise RuntimeError("MODRINTH_PUBLISH_ENABLED is not true")
    if os.getenv("MODRINTH_AI_DISCLOSURE_CONFIRMED") != "true":
        raise RuntimeError("AI content disclosures not confirmed")
    project = api_json("GET", f"/project/{PROJECT_ID}", token)
    if project.get("id") != PROJECT_ID or project.get("source_url", "").rstrip("/") != PROJECT_SOURCE:
        raise RuntimeError("Project ID/source check failed")
    if project.get("status") != "approved":
        raise RuntimeError("Project is not approved")
    versions = api_json("GET", f"/project/{PROJECT_ID}/version", token)
    for published in versions:
        if published.get("version_number") == meta["version_number"]:
            sha512s = {f.get("hashes", {}).get("sha512") for f in published.get("files", [])}
            if digest in sha512s:
                print("No-op: exact same version already published")
                return
            raise RuntimeError("Version already exists with different JAR bytes; increment mod_version")
    data, ctype = multipart(meta, jar)
    reply = api_json("POST", "/version", token, payload=data, content_type=ctype)
    if not reply.get("id") or reply.get("version_number") != meta["version_number"]:
        raise RuntimeError("Unexpected Modrinth upload response")
    print(f"Published: https://modrinth.com/mod/danmaku-chat/version/{reply['id']}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--target", choices=TARGETS)
    group.add_argument("--batch-dir", help="Folder containing release-<version> subfolders")
    parser.add_argument("--dir", default="build/libs")
    parser.add_argument("--publish", action="store_true")
    args = parser.parse_args()
    version = mod_version()
    if args.target:
        publish_target(args.dir, args.target, version, upload=args.publish)
    else:
        for target in TARGETS:
            publish_target(Path(args.batch_dir) / ("release-" + target), target, version, upload=args.publish)


if __name__ == "__main__":
    try:
        main()
    except (ValueError, RuntimeError, OSError, KeyError, zipfile.BadZipFile) as exc:
        print("ERROR: " + str(exc), file=sys.stderr)
        sys.exit(1)

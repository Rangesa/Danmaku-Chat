#!/usr/bin/env python3
"""Make self-contained version-specific source trees from the canonical multi-version source."""
import argparse
import re
import shutil
from pathlib import Path

VERSIONS = ("1.21.8", "1.21.11", "26.2", "26.3")
ROOT = Path(__file__).resolve().parent.parent

def materialize(version, output, source_sha):
    if version not in VERSIONS:
        raise ValueError(f"Unsupported Minecraft version: {version}")
    output = Path(output).resolve()
    if output == ROOT or ROOT in output.parents and output == ROOT:
        raise ValueError("Refusing to overwrite the source repository")
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)
    for name in (".gitignore", "LICENSE", "settings.gradle", "gradlew", "gradlew.bat"):
        shutil.copy2(ROOT / name, output / name)
    shutil.copytree(ROOT / "src/main/resources/assets", output / "src/main/resources/assets")
    if version.startswith("26."):
        shutil.copytree(ROOT / "src/modern/java", output / "src/main/java")
        shutil.copy2(ROOT / "src/modern/resources/fabric.mod.json",
                     output / "src/main/resources/fabric.mod.json")
    else:
        shutil.copytree(ROOT / "src/main/java", output / "src/main/java")
        for unused in ("ChatHudMixin12111.java",) if version == "1.21.8" else ("ChatHudMixin.java",):
            (output / "src/main/java/com/danmakuchat/mixin" / unused).unlink()
        for name in ("fabric.mod.json", "danmakuchat.mixins.json"):
            shutil.copy2(ROOT / "src/main/resources" / name, output / "src/main/resources" / name)
    gradle = (ROOT / "build.gradle").read_text(encoding="utf-8")
    match = re.search(r"(?ms)^def targets = \[\n(.*?)^\]\n", gradle)
    if match is None:
        raise ValueError("Expected supported build.gradle targets declaration")
    rows = match.group(1).splitlines()
    selected = [r for r in rows if re.match(r"^\s*'" + re.escape(version) + r"'\s*:", r)]
    if len(selected) != 1:
        raise ValueError("Cannot identify the target Gradle dependency set")
    gradle = gradle[:match.start()] + "def targets = [\n" + selected[0] + "\n]\n" + gradle[match.end():]
    old_sources = """if (mc.modern) {
    sourceSets.main.java.setSrcDirs(['src/modern/java'])
    sourceSets.main.resources.setSrcDirs(['src/modern/resources'])
}
"""
    if old_sources not in gradle:
        raise ValueError("Unexpected sourceSets layout in canonical build.gradle")
    gradle = gradle.replace(old_sources, "")
    old_copy = """    if (mc.modern) {
        // Reuse localization and original icon without copying large binary assets.
        from('src/main/resources') { include 'assets/**' }
    } else if (mcTarget == '1.21.11') {"""
    if old_copy not in gradle:
        raise ValueError("Unexpected modern resource copy clause in canonical build.gradle")
    gradle = gradle.replace(old_copy, """    if (mcTarget == '1.21.11') {""")
    (output / "build.gradle").write_text(gradle, encoding="utf-8")
    properties = (ROOT / "gradle.properties").read_text(encoding="utf-8")
    if "mc_target=1.21.8" not in properties:
        raise ValueError("Canonical gradle.properties did not have expected default")
    properties = re.sub(r"(?m)^mc_target=.*$", "mc_target=" + version, properties)
    (output / "gradle.properties").write_text(properties, encoding="utf-8")
    mod_version = re.search(r"(?m)^mod_version=(.*)$", properties).group(1)
    (output / "README.md").write_text(f"""# DanmakuChat — Minecraft {version} source

This branch is an **automatically generated, version-specific source snapshot**.
It contains only the implementation for Minecraft **{version}**, including its own
Gradle configuration, mixin/resources, and the Java/Kotlin source code.

Canonical multi-version source: https://github.com/Rangesa/Danmaku-Chat/tree/feature/fabric-multiversion-26x
Generated from canonical revision: [\`{source_sha}\`](https://github.com/Rangesa/Danmaku-Chat/commit/{source_sha})
Matching immutable version tag: [\`mc-{version}-v{mod_version}\`](https://github.com/Rangesa/Danmaku-Chat/tree/mc-{version}-v{mod_version})
Binary/source JAR release: https://github.com/Rangesa/Danmaku-Chat/releases/tag/v{mod_version}-preview

## Build

Use JDK 25 and Gradle 9.6 (a Java 21 toolchain is used for older Minecraft versions):

\`\`\`bash
gradle clean build
\`\`\`

Output: \`build/libs/DanmakuChat-{version}-{mod_version}.jar\` and matching \`-sources.jar\`.

**Development preview, not a compatibility guarantee.** Compiling does not prove
the game launches or the chat overlay renders correctly. Game testing is still required.

This branch is generated from the canonical source on updates. Edit the canonical source
via its pull requests, not this snapshot branch, to avoid losing your changes.
""", encoding="utf-8")
    return {"target": version, "mod_version": mod_version, "output": str(output)}

def main():
    p = argparse.ArgumentParser()
    p.add_argument("--target", choices=VERSIONS, required=True)
    p.add_argument("--output", required=True)
    p.add_argument("--source-sha", required=True)
    args = p.parse_args()
    print(materialize(args.target, args.output, args.source_sha))

if __name__ == "__main__":
    main()

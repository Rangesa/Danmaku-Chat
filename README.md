# DanmakuChat — Minecraft 26.2 source

This branch is an **automatically generated, version-specific source snapshot**.
It contains only the implementation for Minecraft **26.2**, including its own
Gradle configuration, mixin/resources, and the Java/Kotlin source code.

Canonical multi-version source: https://github.com/Rangesa/Danmaku-Chat/tree/feature/fabric-multiversion-26x
Generated from canonical revision: [\`c518259ac2ed462ee00c8623982cf343251ee01f\`](https://github.com/Rangesa/Danmaku-Chat/commit/c518259ac2ed462ee00c8623982cf343251ee01f)
Matching immutable version tag: [\`mc-26.2-v1.1.0-dev\`](https://github.com/Rangesa/Danmaku-Chat/tree/mc-26.2-v1.1.0-dev)
Binary/source JAR release: https://github.com/Rangesa/Danmaku-Chat/releases/tag/v1.1.0-dev-preview

## Build

Use JDK 25 and Gradle 9.6 (a Java 21 toolchain is used for older Minecraft versions):

\`\`\`bash
gradle clean build
\`\`\`

Output: \`build/libs/DanmakuChat-26.2-1.1.0-dev.jar\` and matching \`-sources.jar\`.

**Development preview, not a compatibility guarantee.** Compiling does not prove
the game launches or the chat overlay renders correctly. Game testing is still required.

This branch is generated from the canonical source on updates. Edit the canonical source
via its pull requests, not this snapshot branch, to avoid losing your changes.

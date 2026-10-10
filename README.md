# DanmakuChat — Minecraft 1.21.11 source

This branch is an **automatically generated, version-specific source snapshot**.
It contains only the implementation for Minecraft **1.21.11**, including its own
Gradle configuration, mixin/resources, and the Java/Kotlin source code.

Canonical multi-version source: https://github.com/Rangesa/Danmaku-Chat/tree/feature/fabric-multiversion-26x
Generated from canonical revision: [\`16e73c8e5c67bb4e29edd724fad0f25cc7f558ec\`](https://github.com/Rangesa/Danmaku-Chat/commit/16e73c8e5c67bb4e29edd724fad0f25cc7f558ec)
Matching immutable version tag: [\`mc-1.21.11-v1.1.0-dev\`](https://github.com/Rangesa/Danmaku-Chat/tree/mc-1.21.11-v1.1.0-dev)
Binary/source JAR release: https://github.com/Rangesa/Danmaku-Chat/releases/tag/v1.1.0-dev-preview

## Build

Use JDK 25 and Gradle 9.6 (a Java 21 toolchain is used for older Minecraft versions):

\`\`\`bash
gradle clean build
\`\`\`

Output: \`build/libs/DanmakuChat-1.21.11-1.1.0-dev.jar\` and matching \`-sources.jar\`.

**Development preview, not a compatibility guarantee.** Compiling does not prove
the game launches or the chat overlay renders correctly. Game testing is still required.

This branch is generated from the canonical source on updates. Edit the canonical source
via its pull requests, not this snapshot branch, to avoid losing your changes.

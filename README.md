# DanmakuChat — Minecraft 26.3 source

This branch is an **automatically generated, version-specific source snapshot**.
It contains only the implementation for Minecraft **26.3**, including its own
Gradle configuration, mixin/resources, and the Java/Kotlin source code.

Canonical multi-version source: https://github.com/Rangesa/Danmaku-Chat/tree/feature/fabric-multiversion-26x
Generated from canonical revision: [\`fc3b507478ca5d378674f3f47621e13f6473683f\`](https://github.com/Rangesa/Danmaku-Chat/commit/fc3b507478ca5d378674f3f47621e13f6473683f)
Matching immutable version tag: [\`mc-26.3-v1.1.1-dev\`](https://github.com/Rangesa/Danmaku-Chat/tree/mc-26.3-v1.1.1-dev)
Binary/source JAR release: https://github.com/Rangesa/Danmaku-Chat/releases/tag/v1.1.1-dev-preview

## Build

Use JDK 25 and Gradle 9.6 (a Java 21 toolchain is used for older Minecraft versions):

\`\`\`bash
gradle clean build
\`\`\`

Output: \`build/libs/DanmakuChat-26.3-1.1.1-dev.jar\` and matching \`-sources.jar\`.

**Development preview, not a compatibility guarantee.** Compiling does not prove
the game launches or the chat overlay renders correctly. Game testing is still required.

This branch is generated from the canonical source on updates. Edit the canonical source
via its pull requests, not this snapshot branch, to avoid losing your changes.

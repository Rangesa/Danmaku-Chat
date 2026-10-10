# DanmakuChat multi-version migration (work in progress)

This is a **draft port**. Versions must not be advertised as supported until CI AND an in-game smoke test pass.
The `main` branch remains the 1.21.8 baseline. Building a target produces its own version-specific JAR.

| Target | Mappings | Java | Status |
|---|---|---|---|
| 1.21.8 | Yarn | 21 | previous version; regression test required |
| 1.21.11 | Yarn | 21 | compile candidate; chat mixins need signature review |
| 26.2 | Mojang official | 25 | compile candidate; new Fabric events/HUD port |
| 26.3 | Mojang official | 25 | compile candidate; newest stable target |

## Builds

Use Gradle 9.6.0 (e.g. via Gradle Actions) and a JDK 25 runtime.
For old targets the Java toolchain is set to 21.

```sh
gradle clean build -Pmc_target=1.21.8
gradle clean build -Pmc_target=1.21.11
gradle clean build -Pmc_target=26.2
gradle clean build -Pmc_target=26.3
```

The checked-in repository does not contain `gradle/wrapper/gradle-wrapper.jar`, so the legacy
`./gradlew` launcher is currently not a reliable build command. CI installs Gradle explicitly.

## Architecture

- `src/main/`: Yarn implementation for 1.21.x, including original Kotlin Mod Menu screen and ChatHud mixins.
- `src/modern/`: Mojang-mapped 26.x implementation, using typed Fabric received-chat events, HUD element registry, and a Java-based Mod Menu screen.
- The 26.x path uses a visible-message filter, not cancellation of incoming signed chat, to preserve vanilla chat history.
- The source duplication is transitional. Once CI passes on all targets, extract renderer-independent lane assignment and configuration into a shared source set.
- One release JAR per MC version. Do not widen `fabric.mod.json` version dependency without corresponding tested compatibility.

## Required smoke tests

1. Load Fabric API + Mod Menu + DanmakuChat with each Minecraft target. No Mixin errors or missing classes.
2. Receive signed player chat and system chat; check filtering, formatting and scroll.
3. Confirm no duplicated messages; actionbar overlays should not be inserted as system chat.
4. Toggle vanilla chat, focus the chat screen, check history and player reporting still work.
5. Verify scroll speed, opacity, font scaling, resize, language and multiple lanes.
6. Test with Sodium/other HUD mods; specifically review ownership of ChatComponent's visible-message filter.
7. Check absence of memory growth from uninitialized queued messages when all lanes are busy.
8. Re-run tests before claiming support for the version.

## Known issues before release

- No build or in-game test has been run by this change's authoring environment.
- The 26.x Mod Menu UI uses increment/cycle buttons while the 1.21.x UI uses sliders.
- The 26.x vanilla-chat visibility filter may collide with other mods that own the same predicate; investigate if tests reveal conflicts.
- Java/Kotlin imports and method signatures must be validated by CI; especially Fabric 26.3 UI rendering and 1.21.11 chat method descriptors.
- Minecraft 26.4 snapshots are intentionally excluded.

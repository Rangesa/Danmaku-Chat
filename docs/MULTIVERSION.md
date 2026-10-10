# DanmakuChat multi-version migration (work in progress)

This is a **draft port**. Versions must not be advertised as supported until CI AND an in-game smoke test pass.
The `main` branch remains the 1.21.8 baseline. Building a target produces its own version-specific JAR.

| Target | Mappings | Java | Actual client GameTest |
|---|---|---|---|
| 1.21.8 | Yarn | 21 | PASS: world/chat Mixin/HUD screenshot |
| 1.21.11 | Yarn | 21 | PASS: world/chat Mixin/HUD screenshot |
| 26.2 | Mojang official | 25 | PASS: world/synthetic chat/HUD screenshot |
| 26.3 | Mojang official | 25 | PASS: world/synthetic chat/HUD screenshot (Mesa lavapipe Vulkan) |

Verified CI: https://github.com/Rangesa/Danmaku-Chat/actions/runs/38049163875

These are **actual rendered Minecraft client sessions**, not just compilation. However,
the test injects synthetic chat (and for legacy targets uses the live ChatHud Mixin):
signed remote-player chat, Mod Menu interaction, other mods and the complete settings
UI have NOT been exhaustively exercised. Keep development releases marked prerelease.

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

- Actual Fabric Client GameTest launches and synthetic HUD screenshots succeeded on all four versions. Full manual, multiplayer and other-mod compatibility checks remain incomplete.
- The 26.x Mod Menu UI uses increment/cycle buttons while the 1.21.x UI uses sliders.
- The 26.x vanilla-chat visibility filter may collide with other mods that own the same predicate; investigate if tests reveal conflicts.
- CI confirms the 1.21.11 ChatHud Mixin and 26.x HUD renderer execute without crashing during the smoke test, not every possible game UI state.
- Minecraft 26.4 snapshots are intentionally excluded.

## Real Minecraft Client GameTest

The dedicated source set is enabled only with `-Penable_client_gametests=true`; it is not embedded in release JARs.

Run locally on Linux with JDK 25, Gradle 9.6, Xvfb and Mesa:

```
LIBGL_ALWAYS_SOFTWARE=1 xvfb-run -a gradle runClientGameTest -Pmc_target=26.2 -Penable_client_gametests=true
```

CI uses Mesa lavapipe for 26.3's RenderPearl Vulkan backend. Screenshots, crash reports and logs are attached to the corresponding GitHub Actions run. It is a hard gate before GitHub Releases, per-version source branch updates and Modrinth automation.

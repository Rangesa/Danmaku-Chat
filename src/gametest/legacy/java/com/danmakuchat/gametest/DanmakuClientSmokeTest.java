package com.danmakuchat.gametest;

import com.danmakuchat.DanmakuChat;
import com.danmakuchat.danmaku.DanmakuManager;
import com.danmakuchat.danmaku.DanmakuMessage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

/**
 * Real Minecraft client verification, not a mocked unit test.
 *
 * Checks Fabric entrypoint, world load, ChatHud Mixin reception,
 * HUD initialization/rendering and saves screenshots for visual review.
 */
@SuppressWarnings("UnstableApiUsage")
public final class DanmakuClientSmokeTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        if (!FabricLoader.getInstance().isModLoaded("danmakuchat")) {
            throw new AssertionError("DanmakuChat is not loaded");
        }

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getClientLevel().waitForChunksRender();
            context.runOnClient(client -> {
                if (client.player == null || client.world == null) {
                    throw new AssertionError("Client world/player missing");
                }
                if (DanmakuChat.getRenderer() == null) {
                    throw new AssertionError("Client mod entrypoint did not register its renderer");
                }
                DanmakuManager.getInstance().clear();
                // Real ChatHud route: verifies the addMessage Mixin is applied,
                // not just that we can call the message manager directly.
                client.inGameHud.getChatHud().addMessage(Text.literal("<GameTest> Danmaku smoke test"));
                if (DanmakuManager.getInstance().getActiveMessages().isEmpty()) {
                    throw new AssertionError("ChatHud Mixin did not deliver message to DanmakuManager");
                }
            });
            context.waitTicks(15);
            context.runOnClient(client -> {
                boolean initialized = DanmakuManager.getInstance().getActiveMessages()
                    .stream().anyMatch(DanmakuMessage::isInitialized);
                if (!initialized) {
                    throw new AssertionError("HUD did not initialize the queued danmaku");
                }
            });
            context.takeScreenshot("danmakuchat-legacy-world-chat");
        }
    }
}

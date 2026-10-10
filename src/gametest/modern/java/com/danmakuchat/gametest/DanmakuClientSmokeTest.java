package com.danmakuchat.gametest;

import com.danmakuchat.DanmakuChat;
import com.danmakuchat.danmaku.DanmakuManager;
import com.danmakuchat.danmaku.DanmakuMessage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

/**
 * Real Minecraft 26.x client startup + world + HUD render smoke test.
 * Synthetic in-game message is injected to verify the rendering pipeline;
 * full signed multiplayer chat events still require a separate integration test.
 */
@SuppressWarnings("UnstableApiUsage")
public final class DanmakuClientSmokeTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        if (!FabricLoader.getInstance().isModLoaded("danmakuchat")) {
            throw new AssertionError("DanmakuChat is not loaded");
        }

        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            // GameTest worldBuilder.create() joins the singleplayer world.
            // Wait for rendering without assuming the renamed context API.
            context.waitTicks(25);
            context.runOnClient(client -> {
                if (client.player == null || client.level == null) {
                    throw new AssertionError("Minecraft world/player missing");
                }
                if (DanmakuChat.getRenderer() == null) {
                    throw new AssertionError("Fabric entrypoint did not register renderer");
                }
                DanmakuManager.getInstance().clear();
                DanmakuManager.getInstance().addMessage(Component.literal("<GameTest> Danmaku smoke test"));
                if (DanmakuManager.getInstance().getActiveMessages().isEmpty()) {
                    throw new AssertionError("Danmaku message did not enter queue");
                }
            });
            context.waitTicks(15);
            context.runOnClient(client -> {
                boolean initialized = DanmakuManager.getInstance().getActiveMessages()
                    .stream().anyMatch(DanmakuMessage::isInitialized);
                if (!initialized) {
                    throw new AssertionError("Modern HUD callback did not initialize the queued danmaku");
                }
            });
            context.takeScreenshot("danmakuchat-modern-world-chat");
        }
    }
}

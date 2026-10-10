package com.danmakuchat;

import com.danmakuchat.command.DanmakuCommand;
import com.danmakuchat.config.DanmakuConfig;
import com.danmakuchat.danmaku.DanmakuManager;
import com.danmakuchat.render.DanmakuRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Modern (26.x) entrypoint using typed Fabric chat events, not ChatHud bytecode injections. */
public final class DanmakuChat implements ClientModInitializer {
    public static final String MOD_ID = "danmakuchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static DanmakuRenderer renderer;

    @Override
    public void onInitializeClient() {
        renderer = new DanmakuRenderer();
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(MOD_ID, "flowing_chat"),
            renderer::render
        );
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, chatType, timestamp) -> {
            if (DanmakuConfig.getInstance().isEnabled()) {
                DanmakuManager.getInstance().addMessage(message);
            }
        });
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            DanmakuConfig config = DanmakuConfig.getInstance();
            if (!overlay && config.isEnabled() && config.shouldShowSystemChat()) {
                DanmakuManager.getInstance().addMessage(message);
            }
        });

        // Only hide rendered lines. Keep the signed vanilla chat history intact and
        // show it when the player opens chat to preserve accessibility and reporting.
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.getChat().setVisibleMessageFilter(message ->
            !DanmakuConfig.getInstance().isEnabled()
                || !DanmakuConfig.getInstance().shouldHideVanillaChat()
                || minecraft.gui.getChat().isChatFocused()
        );

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            DanmakuCommand.register(dispatcher)
        );
        LOGGER.info("DanmakuChat modern Fabric client initialized");
    }

    public static DanmakuRenderer getRenderer() {
        return renderer;
    }
}

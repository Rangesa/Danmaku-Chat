package com.danmakuchat.mixin;

import com.danmakuchat.config.DanmakuConfig;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Minecraft 1.21.11-specific ChatHud draw hook.
 *
 * In this version ChatHud#render has gained a TextRenderer and another boolean
 * parameter. The original 1.21.8 descriptor would fail at game startup.
 */
@Mixin(ChatHud.class)
public class ChatHudMixin12111 {
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;IIIZZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void danmaku$onRender(
        DrawContext context, TextRenderer renderer, int currentTick,
        int mouseX, int mouseY, boolean interactable, boolean extra,
        CallbackInfo ci
    ) {
        DanmakuConfig config = DanmakuConfig.getInstance();
        // Retain the normal chat history when a user opens chat or needs to scroll.
        if (config.isEnabled() && config.shouldHideVanillaChat()
            && !((ChatHud)(Object)this).isChatFocused()) {
            ci.cancel();
        }
    }
}

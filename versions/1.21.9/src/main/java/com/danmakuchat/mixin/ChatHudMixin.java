package com.danmakuchat.mixin;

import com.danmakuchat.config.DanmakuConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * DanmakuChat が有効な場合、バニラ Minecraft チャットを隠すための Mixin。
 *
 * この Mixin は ChatHud の render メソッドをインターセプトして、
 * モッドがアクティブな場合、カスタムの弾幕オーバーレイに置き換わるよう、
 * 標準チャットの描画をキャンセルします。
 */
@Mixin(ChatHud.class)
public class ChatHudMixin {

    /**
     * DanmakuChat が有効な場合、バニラチャット描画をキャンセルします。
     *
     * @param context 描画用のコンテキスト
     * @param currentTick 現在のゲームティック
     * @param mouseX マウスの X 位置
     * @param mouseY マウスの Y 位置
     * @param focused チャットがフォーカスされているか
     * @param ci メソッドをキャンセルするためのコールバック情報
     */
    @Inject(
        method = "render(Lnet/minecraft/client/gui/DrawContext;IIIZ)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onRender(DrawContext context, int currentTick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        DanmakuConfig config = DanmakuConfig.getInstance();

        // DanmakuChat が有効かつバニラチャットを隠すように設定されている場合、
        // バニラチャット描画をキャンセル
        if (config.isEnabled() && config.shouldHideVanillaChat()) {
            ci.cancel();
        }
    }
}

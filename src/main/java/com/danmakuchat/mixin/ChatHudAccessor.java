package com.danmakuchat.mixin;

import com.danmakuchat.danmaku.DanmakuManager;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ChatHud に追加されるチャットメッセージをインターセプトするための Mixin。
 *
 * すべてのチャットメッセージをキャプチャし、
 * 弾幕オーバーレイに表示するために DanmakuManager に転送します。
 */
@Mixin(ChatHud.class)
public class ChatHudAccessor {

    /**
     * チャットに追加されているメッセージをインターセプトします。
     * 3 引数の公開メソッド addMessage をフックして、すべてのチャットメッセージをキャプチャします。
     *
     * @param message チャットメッセージテキスト
     * @param signature メッセージシグネチャ（nullable）
     * @param indicator メッセージインジケーター（nullable）
     * @param ci コールバック情報
     */
    @Inject(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD")
    )
    private void onAddMessage(@NotNull Text message, @Nullable MessageSignatureData signature, @Nullable MessageIndicator indicator, CallbackInfo ci) {
        // このメッセージがユーザーメッセージかシステムメッセージかを判定
        String messageText = message.getString();
        boolean isUserMessage = isUserChatMessage(messageText);

        // 設定を確認して、このメッセージタイプを表示するかどうかをチェック
        com.danmakuchat.config.DanmakuConfig config = com.danmakuchat.config.DanmakuConfig.getInstance();

        if (isUserMessage && !config.shouldShowUserChat()) {
            // ユーザーメッセージだが、ユーザーメッセージが無効
            return;
        }

        if (!isUserMessage && !config.shouldShowSystemChat()) {
            // システムメッセージだが、システムメッセージが無効
            return;
        }

        // メッセージを弾幕マネージャーに転送
        DanmakuManager.getInstance().addMessage(message);
    }

    /**
     * メッセージがユーザー（プレイヤーチャット）またはシステムからのものかを検出します。
     * ユーザーメッセージは通常、パターン: <プレイヤー名> メッセージ に従います。
     *
     * @param messageText メッセージのプレーンテキスト
     * @return ユーザーメッセージの場合は true、システムメッセージの場合は false
     */
    private boolean isUserChatMessage(String messageText) {
        // ユーザーチャットメッセージのパターン: <プレイヤー名> メッセージ
        // このプレックスマッチは次のパターンにマッチします：
        // < で開始、その後に少なくとも 1 つの > 以外の文字、その後に >、その後にスペースとコンテンツ
        return messageText.matches("^<[^>]+> .+$");
    }
}

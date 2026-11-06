package com.danmakuchat.mixin;

import com.danmakuchat.danmaku.DanmakuManager;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
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
@Mixin(ChatComponent.class)
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
        method = "addPlayerMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
        at = @At("HEAD")
    )
    private void onAddMessage(@NotNull Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag indicator, CallbackInfo ci) {
        DanmakuManager.getInstance().addMessage(message);
    }

    @Inject(method = {"addClientSystemMessage", "addServerSystemMessage"}, at = @At("HEAD"))
    private void onSystemMessage(Component message, CallbackInfo ci) {
        // このメッセージがユーザーメッセージかシステムメッセージかを判定
        String messageText = message.getString();
        boolean isUserMessage = isUserChatMessage(messageText);

        // 設定を確認して、このメッセージタイプを表示するかどうかをチェック
        com.danmakuchat.config.DanmakuConfig config = com.danmakuchat.config.DanmakuConfig.getInstance();

        // ユーザーチャットの表示設定を削除したため、ユーザーチャットは常に弾幕として表示される
        // if (isUserMessage && !config.shouldShowUserChat()) {
        //     // ユーザーメッセージだが、ユーザーメッセージが無効
        //     return;
        // }

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

package com.danmakuchat;

import com.danmakuchat.command.DanmakuCommand;
import com.danmakuchat.render.DanmakuRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DanmakuChat - Minecraft 用ニコニコスタイル弾幕チャットオーバーレイ
 *
 * このモッドは標準的な Minecraft チャットを、ニコニコ動画のコメントに似た
 * 弾幕スタイルのオーバーレイに置き換えます。
 */
public class DanmakuChat implements ClientModInitializer {
    public static final String MOD_ID = "danmakuchat";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static DanmakuRenderer renderer;

    @Override
    public void onInitializeClient() {
        LOGGER.info("DanmakuChat を初期化中 - チャットを流しましょう！");

        // レンダラーを初期化
        renderer = new DanmakuRenderer();

        // HudRenderCallback API を使用して弾幕レンダラーを HUD に登録
        HudRenderCallback.EVENT.register(renderer::render);

        // クライアントコマンドを登録
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            DanmakuCommand.register(dispatcher);
        });

        LOGGER.info("DanmakuChat の初期化が完了しました！レンダラーとコマンドが登録されました。");
    }

    public static DanmakuRenderer getRenderer() {
        return renderer;
    }
}

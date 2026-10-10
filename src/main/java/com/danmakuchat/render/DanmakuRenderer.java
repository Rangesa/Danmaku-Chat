package com.danmakuchat.render;

import com.danmakuchat.config.DanmakuConfig;
import com.danmakuchat.danmaku.DanmakuManager;
import com.danmakuchat.danmaku.DanmakuMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 弾幕メッセージをスクリーンに描画します。
 *
 * ニコニコ動画の弾幕システムに影響を受けた、
 * 流れるチャットメッセージの視覚的表示を実装します。
 */
public class DanmakuRenderer {
    private static final int LANE_HEIGHT = 20;
    private static final int TOP_MARGIN = 10;

    private long lastFrameTime = -1;

    public DanmakuRenderer() {
    }

    /**
     * すべてのアクティブな弾幕メッセージを描画します。
     *
     * @param context 描画コンテキスト
     * @param tickCounter アニメーション用レンダリングティックカウンター
     */
    public void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        DanmakuConfig config = DanmakuConfig.getInstance();

        if (!config.isEnabled()) {
            return;
        }

        // リアルタイム（ナノ秒）を使用してデルタタイムを計算
        long currentTime = System.nanoTime();
        float deltaTimeSeconds;

        if (lastFrameTime == -1) {
            // 最初のフレーム、60 FPSと仮定
            deltaTimeSeconds = 1.0f / 60.0f;
        } else {
            // 実際の時間差を計算
            deltaTimeSeconds = (currentTime - lastFrameTime) / 1_000_000_000.0f;
            // 合理的な値に制限（大きなジャンプを防止）
            deltaTimeSeconds = Math.min(deltaTimeSeconds, 0.1f);
        }
        lastFrameTime = currentTime;

        // すべてのメッセージを更新
        DanmakuManager manager = DanmakuManager.getInstance();
        manager.update(deltaTimeSeconds);

        // Minecraft インスタンスを取得
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }

        // スクリーン寸法を取得
        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        // 描画状態を保存
        context.pose().pushMatrix();

        try {
            // 各メッセージを描画
            List<DanmakuMessage> messages = manager.getActiveMessages();
            Font textRenderer = client.font;

            for (DanmakuMessage message : messages) {
            // 最初のレンダリング前に位置を初期化
            if (!message.isInitialized()) {
                // フォントサイズ設定を取得
                float fontSize = config.getFontSize();

                // テキスト幅を測定（フォントサイズを考慮）
                Component text = message.getMessage();
                int baseTextWidth = textRenderer.width(text);
                int scaledTextWidth = (int) (baseTextWidth * fontSize);
                message.setTextWidth(scaledTextWidth);

                // ① 速度の計算と設定 (screenWidth を使用して速度を確定)
                message.calculateSpeed(screenWidth);

                // ② レーンの割り当てと衝突回避アルゴリズム
                int bestLane = manager.findBestLane(screenWidth, scaledTextWidth, message.getCalculatedSpeed());

                if (bestLane != -1) {
                    // レーンを割り当て
                    message.setLane(bestLane);

                    // 位置を設定
                    message.setPosX(screenWidth); // 右端から開始
                    message.setPosY(TOP_MARGIN + bestLane * LANE_HEIGHT);

                    // レーン追跡を更新
                    manager.updateLaneInfo(bestLane, message);

                    // 初期化済みとしてマーク
                    message.setInitialized(true);
                } else {
                    // 利用可能なレーンがない - このメッセージをスキップ
                    continue;
                }
            }

            // 初期化されていない場合は描画をスキップ
            if (!message.isInitialized()) {
                continue;
            }

            // メッセージの寸法を取得（キャッシュされた幅を使用）
            Component text = message.getMessage();

            // 位置を計算
            int x = (int) message.getPosX();
            int y = (int) message.getPosY();

            // 設定から不透明度を取得（0.0 - 1.0）
            float opacity = config.getOpacity();
            int alpha = (int) (opacity * 255);

            // 設定可能な不透明度を持つ白色テキスト
            int textColor = (alpha << 24) | 0x00FFFFFF;

            // フォントサイズ設定を取得
            float fontSize = config.getFontSize();

            // フォントサイズをスケール適用するためにマトリックスをプッシュ
            context.pose().pushMatrix();
            context.pose().scale(fontSize, fontSize);

            // スケール後の座標を計算（スケール適用後の座標系に変換）
            int scaledX = (int) (x / fontSize);
            int scaledY = (int) (y / fontSize);

            // 視認性のための影付きテキストを描画
            context.text(textRenderer, text, scaledX, scaledY, textColor, true);

            // マトリックスを復元
            context.pose().popMatrix();
        }
        } finally {
            // 描画状態を復元
            context.pose().popMatrix();
        }
    }
}

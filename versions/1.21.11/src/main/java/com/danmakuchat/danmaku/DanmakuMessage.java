package com.danmakuchat.danmaku;

import com.danmakuchat.config.DanmakuConfig;
import net.minecraft.text.Text;

/**
 * Represents a single danmaku message that flows across the screen.
 */
public class DanmakuMessage {
    private final Text message;
    private final long creationTime;
    private float posX;
    private float posY;
    private float calculatedSpeed;
    private final float targetDuration;
    private float speed;
    private int lane;
    private boolean initialized = false;
    private int textWidth = 0;  // Measured width of the text


    // コンストラクタを変更
    public DanmakuMessage(Text message, float targetDuration) {
        this.message = message;
        this.creationTime = System.currentTimeMillis();
        this.targetDuration = targetDuration; // 目標時間を保持
        // speedは初期化時に計算しない
        this.lane = -1;
        this.posX = -1;
        this.calculatedSpeed = 0.0f;
    }

    public Text getMessage() {
        return message;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public float getPosX() {
        return posX;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public float getPosY() {
        return posY;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public float getSpeed() {
        return speed;
    }

    public int getLane() {
        return lane;
    }

    public void setLane(int lane) {
        this.lane = lane;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public int getTextWidth() {
        return textWidth;
    }

    public void setTextWidth(int textWidth) {
        this.textWidth = textWidth;
    }

    public boolean isExpired(float maxDuration) {
        long currentTime = System.currentTimeMillis();
        return (currentTime - creationTime) > (maxDuration * 1000);
    }
    // 新しいメソッド：速度を計算し、フィールドに格納する
    public void calculateSpeed(int screenWidth) {
        // D = W_screen + W_text
        float totalDistance = (float) screenWidth + (float) this.textWidth;

        // V = D / T_target (pixels per second)
        // さらにコンフィグのscrollSpeedを乗算してユーザーが調整できるようにする
        float speedMultiplier = DanmakuConfig.getInstance().getScrollSpeed();
        this.calculatedSpeed = (totalDistance / this.targetDuration) * speedMultiplier;
    }

    /**
     * 計算された速度を取得します。
     *
     * @return 計算された速度（ピクセル/秒）
     */
    public float getCalculatedSpeed() {
        return this.calculatedSpeed;
    }
    public void update(float deltaTimeSeconds) {
        // 更新には計算された速度を使用
        posX -= calculatedSpeed * deltaTimeSeconds;
    }

    public boolean isOffScreen(int textWidth) {
        // メッセージが左端を超えて移動したときに画面外になります
        return posX + textWidth < 0;
    }
}

package com.danmakuchat.danmaku;

import com.danmakuchat.config.DanmakuConfig;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 弾幕メッセージキューとレーン割り当てを管理します。
 *
 * ニコニコ動画に影響を受けた衝突回避アルゴリズムを実装し、
 * メッセージの重複を防ぎながら可読性を保ちます。
 */
public class DanmakuManager {
    private static DanmakuManager instance;

    private final List<DanmakuMessage> activeMessages = new ArrayList<>();
    private final List<LaneInfo> lanes = new ArrayList<>();

    private static class LaneInfo {
        int laneIndex;
        DanmakuMessage lastMessage;  // このレーン内の最後のメッセージへの参照

        LaneInfo(int index) {
            this.laneIndex = index;
            this.lastMessage = null;
        }
    }

    private DanmakuManager() {
        initializeLanes();
    }

    public static DanmakuManager getInstance() {
        if (instance == null) {
            instance = new DanmakuManager();
        }
        return instance;
    }

    private void initializeLanes() {
        DanmakuConfig config = DanmakuConfig.getInstance();
        int maxLanes = config.getMaxLanes();

        lanes.clear();
        for (int i = 0; i < maxLanes; i++) {
            lanes.add(new LaneInfo(i));
        }
    }

    /**
     * Add a new message to the danmaku system.
     * Lane assignment is deferred until rendering (when text width can be measured).
     *
     * @param message The text message to display
     */
    public void addMessage(Text message) {
        DanmakuConfig config = DanmakuConfig.getInstance();
        if (!config.isEnabled()) {
            return;
        }

        // 固定速度ではなく、目標表示時間T_targetを渡す
        float targetDuration = config.getDisplayDuration();

        // DanmakuMessageのコンストラクタを変更（速度 -> 目標時間）
        DanmakuMessage danmaku = new DanmakuMessage(message, targetDuration);

        activeMessages.add(danmaku);
    }

    /**
     * ニコニコ風の衝突回避アルゴリズムを使用して、最適な利用可能なレーンを見つけます。
     * 修正点: ベストスコア方式を削除し、上から順（レーンインデックスが小さい順）に
     * 最初に見つかった衝突しないレーンを即座に返します。
     *
     * @param screenWidth 画面幅（ピクセル）
     * @param newMessageWidth 新しいメッセージの幅（ピクセル）
     * @param newCalculatedSpeed 新しいメッセージの計算された速度
     * @return レーンインデックス、または利用可能なレーンがない場合は -1
     */
    public int findBestLane(int screenWidth, int newMessageWidth, float newCalculatedSpeed) {
        int maxLanes = DanmakuConfig.getInstance().getMaxLanes();

        if (lanes.size() != maxLanes) {
            initializeLanes();
        }

        final float MIN_SPACING = 5.0f;

        // レーンはインデックスの昇順（上から下）に並んでいるため、
        // ループで上から順にチェックすることで、垂直方向の優先度が保証されます。

        for (LaneInfo lane : lanes) {

            // --- 1. 空きレーンの即時採用 (最優先) ---
            if (lane.lastMessage == null || !lane.lastMessage.isInitialized()) {
                return lane.laneIndex; // 最初に空きレーンが見つかったら即座に採用
            }

            DanmakuMessage prevMessage = lane.lastMessage;
            float prevPosX = prevMessage.getPosX();
            int prevMessageWidth = prevMessage.getTextWidth();
            float prevCalculatedSpeed = prevMessage.getCalculatedSpeed();

            // --- 2. 初期衝突チェック (右端での衝突回避) ---
            if (prevPosX + prevMessageWidth + MIN_SPACING > screenWidth) {
                 continue;
            }

            // --- 3. 追い越し衝突チェック ---
            if (newCalculatedSpeed > prevCalculatedSpeed) {

                float relativeSpeed = newCalculatedSpeed - prevCalculatedSpeed;
                // Prevの終点: prevPosX + prevMessageWidth
                // Newの始点: screenWidth
                float distanceToClose = screenWidth - (prevPosX + prevMessageWidth) + MIN_SPACING;

                if (distanceToClose > 0) {
                    float timeToCollision = distanceToClose / relativeSpeed;
                    float prevTimeToExitScreen = (prevPosX + prevMessageWidth) / prevCalculatedSpeed;

                    if (timeToCollision < prevTimeToExitScreen) {
                        continue; // 衝突予測があるためスキップ
                    }
                }
            }

            // --- 4. 適合レーンの採用 (垂直優先を保証) ---
            // 衝突回避の条件をすべてパスした場合、このレーンは利用可能な最も上のレーンです。
            return lane.laneIndex;
        }

        // すべてのレーンが埋まっているか、衝突するため利用できない
        return -1;
    }

    /**
     * Update lane information after assigning a message to a lane.
     *
     * @param laneIndex The index of the lane
     * @param message The message assigned to this lane
     */
    public void updateLaneInfo(int laneIndex, DanmakuMessage message) {
        if (laneIndex >= 0 && laneIndex < lanes.size()) {
            LaneInfo lane = lanes.get(laneIndex);
            lane.lastMessage = message;
        }
    }

    /**
     * すべてのアクティブなメッセージを更新し、画面外のものを削除します。
     *
     * @param deltaTimeSeconds 前回の更新からの経過時間（秒単位）
     */
    public void update(float deltaTimeSeconds) {
        Iterator<DanmakuMessage> iterator = activeMessages.iterator();
        while (iterator.hasNext()) {
            DanmakuMessage message = iterator.next();

            // 初期化されている場合のみ更新
            if (message.isInitialized()) {
                message.update(deltaTimeSeconds);

                // 画面外のメッセージを削除（初期化されたメッセージのみ）
                // メッセージは右端（posX + textWidth）が左端（x < 0）を超えたときに削除されます
                if (message.isOffScreen(message.getTextWidth())) {
                    iterator.remove();
                }
            }
            // 初期化されていないメッセージは削除しない - 最初にレンダリングされる機会が必要です
        }
    }

    public List<DanmakuMessage> getActiveMessages() {
        return new ArrayList<>(activeMessages);
    }

    public void clear() {
        activeMessages.clear();
    }
}

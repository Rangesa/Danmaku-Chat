package com.danmakuchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration manager for DanmakuChat.
 * Handles settings for danmaku display behavior.
 */
public class DanmakuConfig {
    private static DanmakuConfig instance;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("danmakuchat.json");
    public static final Logger LOGGER = LoggerFactory.getLogger("DanmakuChat");

    // Display settings
    private boolean enabled = true;
    private boolean hideVanillaChat = true;
    private float scrollSpeed = 1.0f;
    private float displayDuration = 5.0f;
    private int maxLanes = 10;
    private float opacity = 0.8f;
    private float fontSize = 1.0f;

    // External chat integration
    private boolean discordIntegration = false;

    // Message filtering settings
    private boolean showSystemChat = false;  // System messages OFF by default

    private DanmakuConfig() {}

    public static DanmakuConfig getInstance() {
        if (instance == null) {
            instance = load();
        }
        return instance;
    }

    /**
     * Loads the configuration from the file, or creates a default one if it doesn't exist.
     */
    private static DanmakuConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                DanmakuConfig config = GSON.fromJson(json, DanmakuConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load DanmakuChat config: " + e.getMessage());
            }
        }
        // If the file doesn't exist or loading fails, return default settings
        DanmakuConfig defaultConfig = new DanmakuConfig();
        defaultConfig.save();
        return defaultConfig;
    }

    /**
     * Saves the current configuration to the file.
     */
    public void save() {
        try {
            String json = GSON.toJson(this);
            Files.writeString(CONFIG_PATH, json);
        } catch (IOException e) {
            LOGGER.error("Failed to save DanmakuChat config: " + e.getMessage());
        }
    }

    // Getters
    public boolean isEnabled() { return enabled; }
    public boolean shouldHideVanillaChat() { return hideVanillaChat; }
    public float getScrollSpeed() { return scrollSpeed; }
    public float getDisplayDuration() { return displayDuration; }
    public int getMaxLanes() { return maxLanes; }
    public float getOpacity() { return opacity; }
    public float getFontSize() { return fontSize; }
    public boolean isDiscordIntegrationEnabled() { return discordIntegration; }
    public boolean shouldShowSystemChat() { return showSystemChat; }

    // Setters (auto-save on change)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        save();
    }
    public void setHideVanillaChat(boolean hide) {
        this.hideVanillaChat = hide;
        save();
    }
    public void setScrollSpeed(float speed) {
        this.scrollSpeed = Math.max(0.1f, Math.min(5.0f, speed));
        save();
    }
    public void setDisplayDuration(float duration) {
        this.displayDuration = Math.max(1.0f, Math.min(30.0f, duration));
        save();
    }
    public void setMaxLanes(int lanes) {
        this.maxLanes = Math.max(1, Math.min(20, lanes));
        save();
    }
    public void setOpacity(float opacity) {
        this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
        save();
    }
    public void setFontSize(float size) {
        this.fontSize = Math.max(0.5f, Math.min(2.0f, size));
        save();
    }
    public void setDiscordIntegration(boolean enabled) {
        this.discordIntegration = enabled;
        save();
    }
    public void setShowSystemChat(boolean show) {
        this.showSystemChat = show;
        save();
    }
}

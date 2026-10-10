package com.danmakuchat.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;

/**
 * Standalone Mojang-mapped Mod Menu integration for 26.x.
 * The old Kotlin/Yarn screen stays untouched for 1.21.x builds.
 */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DanmakuSettings::new;
    }

    private static final class DanmakuSettings extends Screen {
        private final Screen parent;
        private final DanmakuConfig config = DanmakuConfig.getInstance();

        private DanmakuSettings(Screen parent) {
            super(Component.translatable("danmakuchat.config.title"));
            this.parent = parent;
        }
        @Override
        protected void init() {
            int x = width / 2 - 100;
            int y = 28;
            addRenderableWidget(Button.builder(toggle("Enabled", config.isEnabled()),
                b -> { config.setEnabled(!config.isEnabled()); rebuildWidgets(); }).bounds(x,y,200,20).build());
            y += 24;
            addRenderableWidget(Button.builder(toggle("System chat", config.shouldShowSystemChat()),
                b -> { config.setShowSystemChat(!config.shouldShowSystemChat()); rebuildWidgets(); }).bounds(x,y,200,20).build());
            y += 24;
            addRenderableWidget(Button.builder(toggle("Vanilla chat", !config.shouldHideVanillaChat()),
                b -> { config.setHideVanillaChat(!config.shouldHideVanillaChat()); rebuildWidgets(); }).bounds(x,y,200,20).build());
            y += 28;
            addRenderableWidget(Button.builder(Component.literal("Speed: " + fmt(config.getScrollSpeed()) + "  (+0.1)"),
                b -> { float value = config.getScrollSpeed() >= 5 ? 0.1f : config.getScrollSpeed()+0.1f; config.setScrollSpeed(value); rebuildWidgets(); }).bounds(x,y,200,20).build());
            y += 24;
            addRenderableWidget(Button.builder(Component.literal("Opacity: " + fmt(config.getOpacity()) + "  (+0.1)"),
                b -> { float value = config.getOpacity() >= 1 ? 0 : config.getOpacity()+0.1f; config.setOpacity(value); rebuildWidgets(); }).bounds(x,y,200,20).build());
            y += 24;
            addRenderableWidget(Button.builder(Component.literal("Lanes: " + config.getMaxLanes() + "  (+1)"),
                b -> { int value = config.getMaxLanes() >= 20 ? 1 : config.getMaxLanes()+1; config.setMaxLanes(value); rebuildWidgets(); }).bounds(x,y,200,20).build());
            y += 24;
            addRenderableWidget(Button.builder(Component.literal("Font size: " + fmt(config.getFontSize()) + "  (+0.1)"),
                b -> { float value = config.getFontSize() >= 2 ? 0.5f : config.getFontSize()+0.1f; config.setFontSize(value); rebuildWidgets(); }).bounds(x,y,200,20).build());
            y += 32;
            addRenderableWidget(Button.builder(Component.translatable("gui.done"),
                b -> onClose()).bounds(x,y,200,20).build());
        }

        private void rebuildWidgets() {
            clearWidgets();
            init();
        }
        private static Component toggle(String title, boolean value) {
            return Component.literal(title + ": " + (value ? "ON" : "OFF"));
        }
        private static String fmt(float value) {
            return String.format(Locale.ROOT, "%.1f", value);
        }
        @Override
        public void onClose() {
            config.save();
            Minecraft.getInstance().setScreen(parent);
        }
    }
}

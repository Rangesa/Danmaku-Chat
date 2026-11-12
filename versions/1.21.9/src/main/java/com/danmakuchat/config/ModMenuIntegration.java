package com.danmakuchat.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DanmakuConfigScreen::new;
    }

    public static class DanmakuConfigScreen extends Screen {
        private final Screen parent;
        private final DanmakuConfig config = DanmakuConfig.getInstance();

        public DanmakuConfigScreen(Screen parent) {
            super(Text.translatable("danmakuchat.config.title"));
            this.parent = parent;
        }

        private Text enableText(String key, boolean enabled) {
            return Text.translatable(key).append(": ").append(Text.translatable(
                enabled ? "danmakuchat.value.enabled" : "danmakuchat.value.disabled"));
        }

        private void slider(int y, String key, double initial,
                            DoubleFunction<String> label, DoubleConsumer setter) {
            addDrawableChild(new SliderWidget(width / 2 - 100, y, 200, 20,
                    Text.empty(), Math.clamp(initial, 0.0, 1.0)) {
                { updateMessage(); }
                @Override protected void updateMessage() {
                    setMessage(Text.translatable(key).append(": " + label.apply(value)));
                }
                @Override protected void applyValue() { setter.accept(value); }
            });
        }

        @Override protected void init() {
            int x = width / 2 - 100;
            addDrawableChild(ButtonWidget.builder(enableText("danmakuchat.config.enabled", config.isEnabled()), b -> {
                config.setEnabled(!config.isEnabled());
                b.setMessage(enableText("danmakuchat.config.enabled", config.isEnabled()));
            }).dimensions(x, 40, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(enableText("danmakuchat.config.system_chat", config.shouldShowSystemChat()), b -> {
                config.setShowSystemChat(!config.shouldShowSystemChat());
                b.setMessage(enableText("danmakuchat.config.system_chat", config.shouldShowSystemChat()));
            }).dimensions(x, 65, 200, 20).build());
            addDrawableChild(ButtonWidget.builder(enableText("danmakuchat.config.vanilla_chat", !config.shouldHideVanillaChat()), b -> {
                config.setHideVanillaChat(!config.shouldHideVanillaChat());
                b.setMessage(enableText("danmakuchat.config.vanilla_chat", !config.shouldHideVanillaChat()));
            }).dimensions(x, 90, 200, 20).build());
            slider(125, "danmakuchat.config.speed", (config.getScrollSpeed() - 0.1) / 4.9,
                v -> String.format(Locale.ROOT, "%.1f", 0.1 + v * 4.9),
                v -> config.setScrollSpeed((float)(0.1 + v * 4.9)));
            slider(150, "danmakuchat.config.opacity", config.getOpacity(),
                v -> String.format(Locale.ROOT, "%.2f", v), v -> config.setOpacity((float)v));
            slider(175, "danmakuchat.config.lanes", (config.getMaxLanes() - 1) / 19.0,
                v -> Long.toString(Math.round(1 + v * 19)), v -> config.setMaxLanes((int)Math.round(1 + v * 19)));
            slider(200, "danmakuchat.config.font_size", (config.getFontSize() - 0.5) / 1.5,
                v -> String.format(Locale.ROOT, "%.2f", 0.5 + v * 1.5),
                v -> config.setFontSize((float)(0.5 + v * 1.5)));
            addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), b -> close())
                .dimensions(x, 255, 200, 20).build());
        }

        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 20, 0xFFFFFFFF);
            super.render(context, mouseX, mouseY, delta);
        }

        @Override public void close() {
            config.save();
            if (client != null) client.setScreen(parent);
        }
    }
}

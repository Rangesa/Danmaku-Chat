package com.danmakuchat.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text
import kotlin.math.roundToInt

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent: Screen? -> DanmakuConfigScreen(parent) }
    }

    class DanmakuConfigScreen(private val parent: Screen?) : Screen(Text.translatable("danmakuchat.config.title")) {
        private val config: DanmakuConfig = DanmakuConfig.getInstance()

        private fun getEnableText(key: String, enabled: Boolean): Text {
            val status = if (enabled) "danmakuchat.value.enabled" else "danmakuchat.value.disabled"
            return Text.translatable(key).append(": ").append(Text.translatable(status))
        }

        override fun init() {
            val centerX = this.width / 2
            val startY = 40
            val buttonWidth = 200
            val buttonHeight = 20
            val spacing = 25
            var y = startY

            // Enable/Disable toggle
            addDrawableChild(
                ButtonWidget.builder(
                    getEnableText("danmakuchat.config.enabled", config.isEnabled)
                ) { button ->
                    config.isEnabled = !config.isEnabled
                    button.message = getEnableText("danmakuchat.config.enabled", config.isEnabled)
                }.dimensions(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight).build()
            )
            y += spacing

            // System Chat toggle
            addDrawableChild(
                ButtonWidget.builder(
                    getEnableText("danmakuchat.config.system_chat", config.shouldShowSystemChat())
                ) { button ->
                    config.setShowSystemChat(!config.shouldShowSystemChat())
                    button.message = getEnableText("danmakuchat.config.system_chat", config.shouldShowSystemChat())
                }.dimensions(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight).build()
            )
            y += spacing

            // Hide Vanilla Chat toggle
            addDrawableChild(
                ButtonWidget.builder(
                    getEnableText("danmakuchat.config.vanilla_chat", !config.shouldHideVanillaChat())
                ) { button ->
                    config.setHideVanillaChat(!config.shouldHideVanillaChat())
                    button.message = getEnableText("danmakuchat.config.vanilla_chat", !config.shouldHideVanillaChat())
                }.dimensions(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight).build()
            )
            y += spacing + 10

            // Scroll Speed slider
            addDrawableChild(object : SliderWidget(
                centerX - buttonWidth / 2, y, buttonWidth, buttonHeight,
                Text.literal(""), (config.scrollSpeed - 0.1) / (5.0 - 0.1)
            ) {
                init { updateMessage() }
                override fun updateMessage() {
                    val value = 0.1 + this.value * (5.0 - 0.1)
                    message = Text.translatable("danmakuchat.config.speed").append(": ${String.format("%.1f", value)}")
                }
                override fun applyValue() {
                    val value = 0.1 + this.value * (5.0 - 0.1)
                    config.scrollSpeed = value.toFloat()
                }
            })
            y += spacing

            // Opacity slider
            addDrawableChild(object : SliderWidget(
                centerX - buttonWidth / 2, y, buttonWidth, buttonHeight,
                Text.literal(""), config.opacity.toDouble()
            ) {
                init { updateMessage() }
                override fun updateMessage() {
                    message = Text.translatable("danmakuchat.config.opacity").append(": ${String.format("%.2f", this.value)}")
                }
                override fun applyValue() {
                    config.opacity = this.value.toFloat()
                }
            })
            y += spacing

            // Max Lanes slider
            addDrawableChild(object : SliderWidget(
                centerX - buttonWidth / 2, y, buttonWidth, buttonHeight,
                Text.literal(""), (config.maxLanes - 1) / 19.0
            ) {
                init { updateMessage() }
                override fun updateMessage() {
                    val value = (1 + this.value * 19).roundToInt()
                    message = Text.translatable("danmakuchat.config.lanes").append(": $value")
                }
                override fun applyValue() {
                    val value = (1 + this.value * 19).roundToInt()
                    config.maxLanes = value
                }
            })
            y += spacing

            // Font Size slider
            addDrawableChild(object : SliderWidget(
                centerX - buttonWidth / 2, y, buttonWidth, buttonHeight,
                Text.literal(""), (config.fontSize - 0.5) / (2.0 - 0.5)
            ) {
                init { updateMessage() }
                override fun updateMessage() {
                    val value = 0.5 + this.value * (2.0 - 0.5)
                    message = Text.translatable("danmakuchat.config.font_size").append(": ${String.format("%.2f", value)}")
                }
                override fun applyValue() {
                    val value = 0.5 + this.value * (2.0 - 0.5)
                    config.fontSize = value.toFloat()
                }
            })
            y += spacing + 30 // Make space for Done button

            // Done button
            addDrawableChild(
                ButtonWidget.builder(Text.translatable("gui.done")) {
                    this.client?.setScreen(parent)
                }.dimensions(centerX - buttonWidth / 2, y, buttonWidth, buttonHeight).build()
            )
        }

        override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
            renderBackground(context, mouseX, mouseY, delta)
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF)
            super.render(context, mouseX, mouseY, delta)
        }

        override fun close() {
            config.save()
            this.client?.setScreen(this.parent)
        }
    }
}

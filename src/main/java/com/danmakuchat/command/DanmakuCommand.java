package com.danmakuchat.command;

import com.danmakuchat.config.DanmakuConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * DanmakuChat settings command.
 */
public class DanmakuCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(literal("danmaku")
            .executes(DanmakuCommand::showStatus)
            .then(literal("enable")
                .executes(ctx -> setEnabled(ctx, true)))
            .then(literal("disable")
                .executes(ctx -> setEnabled(ctx, false)))
            .then(literal("system")
                .then(argument("value", BoolArgumentType.bool())
                    .executes(ctx -> setSystemChat(ctx, BoolArgumentType.getBool(ctx, "value")))))
            .then(literal("user")
                .then(argument("value", BoolArgumentType.bool())
                    .executes(ctx -> setUserChat(ctx, BoolArgumentType.getBool(ctx, "value")))))
            .then(literal("speed")
                .then(argument("value", FloatArgumentType.floatArg(0.1f, 5.0f))
                    .executes(ctx -> setSpeed(ctx, FloatArgumentType.getFloat(ctx, "value")))))
            .then(literal("lanes")
                .then(argument("value", IntegerArgumentType.integer(1, 20))
                    .executes(ctx -> setLanes(ctx, IntegerArgumentType.getInteger(ctx, "value")))))
            .then(literal("opacity")
                .then(argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                    .executes(ctx -> setOpacity(ctx, FloatArgumentType.getFloat(ctx, "value")))))
            .then(literal("size")
                .then(argument("value", FloatArgumentType.floatArg(0.5f, 2.0f))
                    .executes(ctx -> setFontSize(ctx, FloatArgumentType.getFloat(ctx, "value")))))
            .then(literal("vanilla")
                .then(argument("value", BoolArgumentType.bool())
                    .executes(ctx -> setVanillaChat(ctx, BoolArgumentType.getBool(ctx, "value")))))
            .then(literal("reload")
                .executes(DanmakuCommand::reload))
        );
    }

    private static Text getEnableStatusText(boolean enabled) {
        return Text.translatable(enabled ? "danmakuchat.value.enabled" : "danmakuchat.value.disabled");
    }

    private static int showStatus(CommandContext<FabricClientCommandSource> ctx) {
        DanmakuConfig config = DanmakuConfig.getInstance();
        FabricClientCommandSource source = ctx.getSource();

        source.sendFeedback(Text.translatable("danmakuchat.command.status.main", getEnableStatusText(config.isEnabled())));
        source.sendFeedback(Text.translatable("danmakuchat.command.status.system", getEnableStatusText(config.shouldShowSystemChat())));
        source.sendFeedback(Text.translatable("danmakuchat.command.status.user", getEnableStatusText(config.shouldShowUserChat())));
        source.sendFeedback(Text.translatable("danmakuchat.command.status.vanilla", getEnableStatusText(!config.shouldHideVanillaChat())));
        source.sendFeedback(Text.translatable("danmakuchat.command.status.speed", config.getScrollSpeed()));
        source.sendFeedback(Text.translatable("danmakuchat.command.status.lanes", config.getMaxLanes()));
        source.sendFeedback(Text.translatable("danmakuchat.command.status.opacity", config.getOpacity()));
        source.sendFeedback(Text.translatable("danmakuchat.command.status.font_size", config.getFontSize()));

        return 1;
    }

    private static int setEnabled(CommandContext<FabricClientCommandSource> ctx, boolean enabled) {
        DanmakuConfig.getInstance().setEnabled(enabled);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.main", getEnableStatusText(enabled)));
        return 1;
    }

    private static int setSystemChat(CommandContext<FabricClientCommandSource> ctx, boolean show) {
        DanmakuConfig.getInstance().setShowSystemChat(show);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.system", getEnableStatusText(show)));
        return 1;
    }

    private static int setUserChat(CommandContext<FabricClientCommandSource> ctx, boolean show) {
        DanmakuConfig.getInstance().setShowUserChat(show);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.user", getEnableStatusText(show)));
        return 1;
    }

    private static int setSpeed(CommandContext<FabricClientCommandSource> ctx, float speed) {
        DanmakuConfig.getInstance().setScrollSpeed(speed);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.speed", speed));
        return 1;
    }

    private static int setLanes(CommandContext<FabricClientCommandSource> ctx, int lanes) {
        DanmakuConfig.getInstance().setMaxLanes(lanes);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.lanes", lanes));
        return 1;
    }

    private static int setOpacity(CommandContext<FabricClientCommandSource> ctx, float opacity) {
        DanmakuConfig.getInstance().setOpacity(opacity);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.opacity", opacity));
        return 1;
    }

    private static int setFontSize(CommandContext<FabricClientCommandSource> ctx, float size) {
        DanmakuConfig.getInstance().setFontSize(size);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.font_size", size));
        return 1;
    }

    private static int setVanillaChat(CommandContext<FabricClientCommandSource> ctx, boolean show) {
        DanmakuConfig.getInstance().setHideVanillaChat(!show);
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.set.vanilla", getEnableStatusText(show)));
        return 1;
    }

    private static int reload(CommandContext<FabricClientCommandSource> ctx) {
        DanmakuConfig.getInstance().save();
        ctx.getSource().sendFeedback(Text.translatable("danmakuchat.command.reload"));
        return 1;
    }
}
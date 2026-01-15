package com.lx862.qomc.util;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;

//? if minecraft: >= 1.21.11 {
/*import net.minecraft.commands.Commands;
*///? }

import java.util.function.Supplier;

public class VersionUtil {
    public static LiteralArgumentBuilder<CommandSourceStack> requireMaxPermissionLevel(LiteralArgumentBuilder<CommandSourceStack> ctx) {
        //? if >= 1.21.11 {
            /*return ctx.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        *///? } else {
            return ctx.requires(source -> source.hasPermission(4));
        //? }
    }

    public static void sendFeedback(CommandSourceStack source, Supplier<Component> getText, boolean broadcastToOp) {
        //? if >= 1.20 {
            source.sendSuccess(getText, broadcastToOp);
        //? } else {
            /*source.sendSuccess(getText.get(), broadcastToOp);
        *///? }
    }

    public static void sendFailure(CommandSourceStack source, Component getText) {
        source.sendFailure(getText);
    }

    public static HoverEvent hoverEventText(Component text) {
        //? if >= 1.21.5 {
            /*return new HoverEvent.ShowText(text);
        *///? } else {
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
        //? }
    }

    public static ClickEvent clickEventSuggestCommand(String command) {
        //? if >= 1.21.5 {
            /*return new ClickEvent.SuggestCommand(command);
        *///? } else {
            return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
        //? }
    }

    public static MutableComponent emptyText() {
        return literalText("");
    }

    public static MutableComponent literalText(String str) {
        //? if >= 1.19 {
            return Component.literal(str);
        //? } else {
            /*return new TextComponent(str);
        *///? }
    }

    public static MutableComponent translatableText(String str) {
        //? if >= 1.19 {
            return Component.translatable(str);
        //? } else {
            /*return new TranslatableComponent(str);
        *///? }
    }
}

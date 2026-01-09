package com.lx862.qomc;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Platform {
    public static void sendFeedback(ServerCommandSource source, Supplier<Text> getText, boolean broadcastToOp) {
        source.sendFeedback(getText, broadcastToOp);
    }

    public static HoverEvent hoverEventText(Text text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }

    public static ClickEvent clickEventSuggestCommand(String command) {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
    }

    public static void registerCommand(Consumer<CommandDispatcher<ServerCommandSource>> commandRegistrationCallback) {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandRegistrationCallback.accept(commandDispatcher);
        }));
    }
}

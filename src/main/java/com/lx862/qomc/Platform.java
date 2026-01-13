package com.lx862.qomc;

import com.lx862.qomc.util.ModInfo;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Platform {
    public static void sendFeedback(CommandSourceStack source, Supplier<Component> getText, boolean broadcastToOp) {
        source.sendSuccess(getText, broadcastToOp);
    }

    public static HoverEvent hoverEventText(Component text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }

    public static ClickEvent clickEventSuggestCommand(String command) {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
    }

    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> commandRegistrationCallback) {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandRegistrationCallback.accept(commandDispatcher);
        }));
    }

    public static boolean modLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public static ModInfo getModInfo(String id) {
        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer(id).get().getMetadata();
        return new ModInfo(modMetadata.getName(), modMetadata.getDescription(), modMetadata.getId());
    }
}

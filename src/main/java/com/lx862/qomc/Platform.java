package com.lx862.qomc;

import com.lx862.qomc.util.ModInfo;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//? if minecraft: <= 1.18.2 {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
*///? } else {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//? }
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;

//? if minecraft: >= 1.21.11 {
/*import net.minecraft.commands.Commands;
*///? }

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Platform {
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

    public static void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> commandRegistrationCallback) {
        //? if >= 1.19 {
            CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
                commandRegistrationCallback.accept(commandDispatcher);
            }));
        //? } else {
            /*CommandRegistrationCallback.EVENT.register((commandDispatcher, b) -> commandRegistrationCallback.accept(commandDispatcher));
        *///? }
    }

    public static boolean modLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public static ModInfo getModInfo(String id) {
        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer(id).get().getMetadata();
        return new ModInfo(modMetadata.getName(), modMetadata.getVersion().getFriendlyString(), modMetadata.getId());
    }

    public static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
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

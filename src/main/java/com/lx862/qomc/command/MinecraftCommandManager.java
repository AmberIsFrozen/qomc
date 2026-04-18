package com.lx862.qomc.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class MinecraftCommandManager implements AbstractCommandManager<CommandSourceStack> {
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> literal(String str) {
        return Commands.literal(str);
    }

    @Override
    public <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type) {
        return Commands.argument(name, type);
    }

    @Override
    public AbstractCommandSource getCommandSource(CommandContext<CommandSourceStack> ctx) {
        return new MinecraftCommandSource(ctx.getSource());
    }

    @Override
    public LiteralArgumentBuilder<CommandSourceStack> requirePermission(LiteralArgumentBuilder<CommandSourceStack> ctx) {
        //? if >= 1.21.11 {
        /*return ctx.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        *///? } else {
        return ctx.requires(source -> source.hasPermission(4));
         //? }
    }
}

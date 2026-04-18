package com.lx862.qomc.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

public interface AbstractCommandManager<S> {
    LiteralArgumentBuilder<S> literal(String str);
    <T> RequiredArgumentBuilder<S, T> argument(String name, ArgumentType<T> type);
    AbstractCommandSource getCommandSource(CommandContext<S> ctx);
    LiteralArgumentBuilder<S> requirePermission(LiteralArgumentBuilder<S> ctx);
}

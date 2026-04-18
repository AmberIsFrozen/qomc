package com.lx862.qomc.xplat.fabric.client;

//? if fabric {
import com.lx862.qomc.command.AbstractCommandManager;
import com.lx862.qomc.command.AbstractCommandSource;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

//? if >= 1.19 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//? } else {
/*import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
*///? }

public class FabricCommandManager implements AbstractCommandManager<FabricClientCommandSource> {
    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> literal(String str) {
        return ClientCommandManager.literal(str);
    }

    @Override
    public <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String name, ArgumentType<T> type) {
        return ClientCommandManager.argument(name, type);
    }

    @Override
    public AbstractCommandSource getCommandSource(CommandContext<FabricClientCommandSource> ctx) {
        return new FabricCommandSource(ctx.getSource());
    }

    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> requirePermission(LiteralArgumentBuilder<FabricClientCommandSource> ctx) {
        return ctx.requires(source -> true);
    }
}
//?}

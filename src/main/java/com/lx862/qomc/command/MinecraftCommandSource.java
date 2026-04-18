package com.lx862.qomc.command;

import com.lx862.qomc.util.VersionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class MinecraftCommandSource implements AbstractCommandSource {
    private final CommandSourceStack commandSourceStack;

    public MinecraftCommandSource(CommandSourceStack commandSourceStack) {
        this.commandSourceStack = commandSourceStack;
    }

    @Override
    public void sendFeedback(Component component, boolean broadcastToOp) {
        VersionUtil.sendFeedback(this.commandSourceStack, () -> component, broadcastToOp);
    }

    @Override
    public void sendFailure(Component component) {
        VersionUtil.sendFailure(this.commandSourceStack, component);
    }
}

package com.lx862.qomc.command;

import net.minecraft.network.chat.Component;

public interface AbstractCommandSource {
    void sendFeedback(Component component, boolean broadcastToOp);
    void sendFailure(Component component);
}

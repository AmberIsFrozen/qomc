package com.lx862.qomc.xplat.fabric.client;

//? if fabric {
import com.lx862.qomc.command.AbstractCommandSource;
//? if >= 1.19 {
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
//? } else {
/*import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
*///? }

import net.minecraft.network.chat.Component;

public class FabricCommandSource implements AbstractCommandSource {
    private final FabricClientCommandSource sourceStack;

    public FabricCommandSource(FabricClientCommandSource sourceStack) {
        this.sourceStack = sourceStack;
    }

    @Override
    public void sendFeedback(Component component, boolean broadcastToOp) {
        this.sourceStack.sendFeedback(component);
    }

    @Override
    public void sendFailure(Component component) {
        this.sourceStack.sendError(component);
    }
}
//?}
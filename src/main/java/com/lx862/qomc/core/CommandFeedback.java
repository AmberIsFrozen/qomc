package com.lx862.qomc.core;

import com.lx862.qomc.command.AbstractCommandSource;
import com.lx862.qomc.util.VersionUtil;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class CommandFeedback {
    private final List<MutableComponent> components = new ArrayList<>();

    public void add(MutableComponent component) {
        this.components.add(component);
    }

    public void add(List<MutableComponent> components) {
        for(MutableComponent mutableComponent : components) {
            add(mutableComponent);
        }
    }

    public void addEmptyLine() {
        add(VersionUtil.emptyText());
    }

    public void send(AbstractCommandSource commandSourceStack, boolean broadcastToOp) {
        for(MutableComponent component : components) {
            commandSourceStack.sendFeedback(component, broadcastToOp);
        }
    }
}

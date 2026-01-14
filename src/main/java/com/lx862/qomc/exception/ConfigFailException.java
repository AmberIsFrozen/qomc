package com.lx862.qomc.exception;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ConfigFailException extends Exception {
    private final Component component;

    public ConfigFailException(MutableComponent component) {
        this.component = component.withStyle(ChatFormatting.RED);
    }

    public Component component() {
        return this.component;
    }
}

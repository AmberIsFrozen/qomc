package com.lx862.qomc.client;

import com.lx862.qomc.Qomc;
import com.lx862.qomc.command.AbstractCommandManager;
import com.mojang.brigadier.CommandDispatcher;

public class QomcClient {
    public static <S> void init(CommandDispatcher<S> dispatcher, AbstractCommandManager<S> commandManager) {
        Qomc.registerCommand(commandManager, dispatcher, true);
    }
}

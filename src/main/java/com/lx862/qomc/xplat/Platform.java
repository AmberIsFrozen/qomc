package com.lx862.qomc.xplat;

//? fabric
import com.lx862.qomc.xplat.fabric.FabricPlatform;
//? neoforge
//import com.lx862.qomc.xplat.neoforge.NeoforgePlatform;
//? forge
//import com.lx862.qomc.xplat.forge.ForgePlatform;

import com.lx862.qomc.util.ModInfo;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface Platform {
    //? fabric
    Platform INSTANCE = new FabricPlatform();
    //? neoforge
    //Platform INSTANCE = new NeoforgePlatform();
    //? forge
    //Platform INSTANCE = new ForgePlatform();

    boolean modLoaded(String id);

    ModInfo getModInfo(String id);

    void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> commandRegistrationCallback);

    Path getConfigPath();
}

package com.lx862.qomc.xplat.neoforge;

//? neoforge {
/*import com.lx862.qomc.Qomc;
import com.lx862.qomc.xplat.Platform;
import com.lx862.qomc.util.ModInfo;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.function.Consumer;

public class NeoforgePlatform implements Platform {

    @Override
    public boolean modLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    @Override
    public ModInfo getModInfo(String id) {
        IModInfo iModInfo = ModList.get().getModContainerById(id).get().getModInfo();
        boolean modHidden = iModInfo.getModProperties().containsKey("hidden");
        return new ModInfo(iModInfo.getDisplayName(), iModInfo.getVersion().toString(), iModInfo.getModId(), modHidden);
    }

    @Override
    public void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> commandRegistrationCallback) {
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, (event) -> {
            commandRegistrationCallback.accept(event.getDispatcher());
        });
    }

    @Override
    public Path getConfigPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
    }
}
*///? }
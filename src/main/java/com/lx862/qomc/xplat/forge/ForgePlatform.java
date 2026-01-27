package com.lx862.qomc.xplat.forge;

//? forge {
/*import com.lx862.qomc.xplat.Platform;
import com.lx862.qomc.util.ModInfo;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.function.Consumer;

public class ForgePlatform implements Platform {

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
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, (event) -> {
            if(event instanceof RegisterCommandsEvent) {
                commandRegistrationCallback.accept(((RegisterCommandsEvent)event).getDispatcher());
            }
        });
    }

    @Override
    public Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get();
    }
}
*///? }
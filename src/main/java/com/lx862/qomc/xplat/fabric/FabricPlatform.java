package com.lx862.qomc.xplat.fabric;

//? if fabric {
import com.lx862.qomc.xplat.Platform;
import com.lx862.qomc.util.ModInfo;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.commands.CommandSourceStack;
//? if minecraft: <= 1.18.2 {
/*import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
*///? } else {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//? }
import java.nio.file.Path;
import java.util.function.Consumer;

public class FabricPlatform implements Platform {
    @Override
    public boolean modLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    @Override
    public ModInfo getModInfo(String id) {
        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer(id).get().getMetadata();
        return new ModInfo(modMetadata.getName(), modMetadata.getVersion().getFriendlyString(), modMetadata.getId());
    }

    @Override
    public Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public void registerCommand(Consumer<CommandDispatcher<CommandSourceStack>> commandRegistrationCallback) {
        //? if >= 1.19 {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            commandRegistrationCallback.accept(commandDispatcher);
        }));
        //? } else {
        /*CommandRegistrationCallback.EVENT.register((commandDispatcher, b) -> commandRegistrationCallback.accept(commandDispatcher));
         *///? }
    }
}
//?}
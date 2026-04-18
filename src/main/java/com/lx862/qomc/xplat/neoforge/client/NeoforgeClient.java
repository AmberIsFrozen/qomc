package com.lx862.qomc.xplat.neoforge.client;

//? neoforge {
/*import com.lx862.qomc.client.QomcClient;
import com.lx862.qomc.command.MinecraftCommandManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = "qomc", dist = Dist.CLIENT)
public class NeoforgeClient {
    public NeoforgeClient(IEventBus eventBus) {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterClientCommand(RegisterClientCommandsEvent event) {
        QomcClient.init(event.getDispatcher(), new MinecraftCommandManager());
    }
}
*///?}
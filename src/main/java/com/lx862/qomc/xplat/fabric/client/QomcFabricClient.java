package com.lx862.qomc.xplat.fabric.client;

//? if fabric {
import com.lx862.qomc.client.QomcClient;
import net.fabricmc.api.ClientModInitializer;
//? if >= 1.19 {
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
//? } else {
/*import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
*///?}

public class QomcFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        //? if >= 1.19 {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            QomcClient.init(dispatcher, new FabricCommandManager());
        });
        //? } else {
        /*QomcClient.init(ClientCommandManager.DISPATCHER, new FabricCommandManager());
        *///?}
    }
}
//?}
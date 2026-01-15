package com.lx862.qomc.xplat.fabric;

//? if fabric {
import com.lx862.qomc.Qomc;
import net.fabricmc.api.ModInitializer;

public class QomcFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Qomc.init();
    }
}
//?}
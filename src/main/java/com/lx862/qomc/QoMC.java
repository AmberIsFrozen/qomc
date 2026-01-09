package com.lx862.qomc;

import com.lx862.qomc.util.QconfUtil;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;

import java.util.*;

public class QoMC implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.registerCommand(commandDispatcher -> {
            Map<ModMetadata, List<Config>> configGroup = new HashMap<>();

            for(Config config : ConfigsImpl.getAll()) {
                String id = QconfUtil.getFamilyOrId(config);
                List<String> probeIds = Arrays.asList(
                        id,
                        id.replace("-", ""),
                        id.replace("_", ""),
                        id.replace("_", "-"),
                        id.replace("-", "_")
                );
                for (String s : probeIds) {
                    if (FabricLoader.getInstance().isModLoaded(s)) {
                        ModMetadata modMetadata = FabricLoader.getInstance().getModContainer(s).get().getMetadata();
                        List<Config> configs = configGroup.computeIfAbsent(modMetadata, k -> new ArrayList<>());
                        configs.add(config);
                        break;
                    }
                }
            }

            for(Map.Entry<ModMetadata, List<Config>> configEntry : configGroup.entrySet()) {
                commandDispatcher.register(QomcCommand.buildModNode(configEntry.getKey(), configEntry.getValue(), commandDispatcher));
            }
        });
    }
}

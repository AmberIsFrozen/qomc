package com.lx862.qomc;

import com.lx862.qomc.util.QconfUtil;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Qomc implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("QoMC");

    @Override
    public void onInitialize() {
        Platform.registerCommand(commandDispatcher -> {
            Map<ModMetadata, List<Config>> configGroup = probeModConfigs();

            for(Map.Entry<ModMetadata, List<Config>> modConfigEntry : configGroup.entrySet()) {
                ModMetadata belongingMod = modConfigEntry.getKey();
                List<Config> modConfigs = modConfigEntry.getValue();
                commandDispatcher.register(
                    ConfigCommands.buildModNode(belongingMod, modConfigs, commandDispatcher)
                );
            }
        });
    }

    private static Map<ModMetadata, List<Config>> probeModConfigs() {
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
                } else {
                    LOGGER.warn("[QoMC] Found config with id {}, but cannot match it with any mod!", id);
                }
            }
        }
        return configGroup;
    }
}

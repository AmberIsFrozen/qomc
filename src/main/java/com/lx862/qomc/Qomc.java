package com.lx862.qomc;

import com.lx862.qomc.config.DemoConfig;
import com.lx862.qomc.util.ModInfo;
import com.lx862.qomc.util.QconfUtil;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.impl.util.ConfigsImpl;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Qomc implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("QoMC");

    @Override
    public void onInitialize() {
        DemoConfig.init();


        Platform.registerCommand(commandDispatcher -> {
            Map<ModInfo, List<Config>> configGroup = probeModConfigs();

            for (Map.Entry<ModInfo, List<Config>> modConfigEntry : configGroup.entrySet()) {
                ModInfo belongingMod = modConfigEntry.getKey();
                List<Config> modConfigs = modConfigEntry.getValue();
                commandDispatcher.register(
                        ConfigCommands.buildModNode(belongingMod, modConfigs, commandDispatcher)
                );
            }
        });
    }

    private static Map<ModInfo, List<Config>> probeModConfigs() {
        Map<ModInfo, List<Config>> configGroup = new HashMap<>();

        for(Config config : ConfigsImpl.getAll()) {
            String configId = QconfUtil.getFamilyOrId(config);
            List<String> probeIds = Arrays.asList(
                    configId,
                    configId.replace("-", ""),
                    configId.replace("_", ""),
                    configId.replace("_", "-"),
                    configId.replace("-", "_")
            );
            boolean modFound = false;
            for (String id : probeIds) {
                if (Platform.modLoaded(id)) {
                    List<Config> configs = configGroup.computeIfAbsent(Platform.getModInfo(id), k -> new ArrayList<>());
                    configs.add(config);
                    modFound = true;
                    break;
                }
            }
            if(modFound) continue;

            Qomc.LOGGER.warn("[QoMC] Found config with id {}, but cannot match it with any mod!", configId);
        }
        return configGroup;
    }
}

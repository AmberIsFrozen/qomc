package com.lx862.qomc.core;

import com.lx862.qomc.util.QconfUtil;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;

public class ConfigTree {
    private final ConfigSectionTree rootSection;

    public static ConfigTree of(Config config) {
        return new ConfigTree(config);
    }

    protected ConfigTree(Config config) {
        this.rootSection = ConfigSectionTree.create();
        generateCommandTree(config);
    }

    private void generateCommandTree(Config config) {
        for(TrackedValue<?> field : config.values()) {
            ConfigSectionTree configSectionTree = this.rootSection;
            if(field.key().length() > 1) {
                for(int i = 0; i < field.key().length()-1; i++) {
                    ValueKey sectionKey = QconfUtil.trimKey(field.key(), field.key().length()-1-i);
                    configSectionTree = configSectionTree.sections().computeIfAbsent(sectionKey, (k) -> ConfigSectionTree.create());
                }
            }

            configSectionTree.fields().add(field);
        }
    }

    public final ConfigSectionTree rootSection() {
        return this.rootSection;
    }
}

package com.lx862.qomc.core;

import com.lx862.qomc.util.QconfUtil;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;

public class ConfigTree {
    private final ConfigSection rootSection;

    public static ConfigTree of(Config config) {
        return new ConfigTree(config);
    }

    protected ConfigTree(Config config) {
        this.rootSection = ConfigSection.create();
        generateCommandTree(config);
    }

    private void generateCommandTree(Config config) {
        for(TrackedValue<?> field : config.values()) {
            ConfigSection configSection = this.rootSection;
            if(field.key().length() > 1) {
                for(int i = 0; i < field.key().length()-1; i++) {
                    ValueKey sectionKey = QconfUtil.trimKey(field.key(), field.key().length()-1-i);
                    configSection = configSection.sections().computeIfAbsent(sectionKey, (k) -> ConfigSection.create());
                }
            }

            configSection.fields().add(field);
        }
    }

    public final ConfigSection rootSection() {
        return this.rootSection;
    }
}

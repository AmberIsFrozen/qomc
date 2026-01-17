package com.lx862.qomc.config;

import com.lx862.qomc.xplat.Platform;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

@DisplayName("Main Config")
public class MainConfig extends ReflectiveConfig {
    public static final MainConfig INSTANCE = MainConfig.createToml(Platform.INSTANCE.getConfigPath(), "qomc", "main", MainConfig.class);

    @Comment("If true, only a single \"/config\" command will be registered which covers all mod.")
    @Comment("If false, a command with the name \"/<mod_id>Config\" will be registered")
    @ChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type.RequiresRestart)
    public final TrackedValue<Boolean> unifiedConfigCommand = value(false);

    public static void init() {
        // Static initialization
    }
}

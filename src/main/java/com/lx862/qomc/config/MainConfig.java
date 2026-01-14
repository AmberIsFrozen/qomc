package com.lx862.qomc.config;

import com.lx862.qomc.Platform;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class MainConfig extends ReflectiveConfig {
    public static final MainConfig INSTANCE = MainConfig.createToml(Platform.getConfigPath(), "qomc", "main", MainConfig.class);

    @Comment("Whether to generate a demo config for demonstration purpose of this mod")
    @Comment("If you do not like the demo.toml file being generated, you may turn this off.")
    @ChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type.RequiresRestart)
    public final TrackedValue<Boolean> generateDemoConfig = value(true);

    public static void init() {
        // Static initialization
    }
}

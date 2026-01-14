package com.lx862.qomc.config;

import com.lx862.qomc.Platform;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Matches;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueListImpl;

import java.util.Arrays;

public class DemoConfig extends ReflectiveConfig {
    private static final DemoConfig INSTANCE = DemoConfig.createToml(Platform.getConfigPath(), "qomc", "demo", DemoConfig.class);

    public final Type type = new Type();
    public final Special special = new Special();

    public static final class Type extends Section {
        public final TrackedValue<Boolean> boolValue = value(false);
        public final TrackedValue<Integer> integerValue = value(0);
        public final TrackedValue<Long> longValue = value(1L);
        public final TrackedValue<Double> doubleValue = value(1d);
        public final TrackedValue<Float> floatValue = value(1f);
        public final TrackedValue<String> stringValue = value("Hello World");

        @Matches("#[0-9A-Fa-f]{6}")
        public final TrackedValue<ValueList<String>> colorListValue = value(new ValueListImpl<>("", Arrays.asList("#00FF00", "#FFFF00")));
    }

    public static final class Special extends Section {

        @Comment("This config value is overriden in code, and the value in the config will not be effective.")
        public final TrackedValue<Integer> overridenValue = value(4);

        public final NestedSection nestedSection = new NestedSection();

        public static final class NestedSection extends Section {
            public final TrackedValue<Boolean> happy = value(true);
        }
    }

    public static void init() {
        // Static initialization
        INSTANCE.special.overridenValue.setOverride(8);
    }
}

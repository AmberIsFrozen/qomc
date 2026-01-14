package com.lx862.qomc.config;

import com.lx862.qomc.Platform;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Matches;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueListImpl;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type;

import java.util.Arrays;

@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class DemoConfig extends ReflectiveConfig {
    private static final DemoConfig INSTANCE = DemoConfig.createToml(Platform.getConfigPath(), "qomc", "demo", DemoConfig.class);

    public final FieldType fieldType = new FieldType();

    @Comment("Demonstration of more special/specific pattern/fields")
    public final Special special = new Special();

    public static final class FieldType extends Section {
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

        @Comment("A list of values making use of the @ChangeWarning annotations, prompting a remark for the user.")
        @Comment("Oh and this is a nested section!")
        public final ChangeWarnings changeWarnings = new ChangeWarnings();

        public static final class ChangeWarnings extends Section {

            @Comment("The \"RequiresRestart\" type marks that a field would only be effective after a game restart.")
            @ChangeWarning(Type.RequiresRestart)
            public final TrackedValue<Boolean> requiresRestart = value(true);

            @Comment("The \"Unsafe\" type marks that this option is not safe to be turned on.")
            @Comment("i.e. Things may go wrong with this option turned on, it is known and that's by design.")
            @ChangeWarning(Type.Unsafe)
            public final TrackedValue<Boolean> unsafe = value(true);

            @Comment("The \"Experimental\" type marks that this option is experimental and might cause issues.")
            @ChangeWarning(Type.Experimental)
            public final TrackedValue<Boolean> experimental = value(true);

            @Comment("The \"Custom\" type allows you to enter a (non-translatable) warning message of your choice to prompt the user.")
            @ChangeWarning(value = Type.Custom, customMessage = "Rawr, look behind you!")
            public final TrackedValue<Boolean> custom = value(true);

            @Comment("The \"CustomTranslatable\" type allows you to enter a translatable warning message of your choice to prompt the user.")
            @ChangeWarning(value = Type.CustomTranslatable, customMessage = "chat.link.warning")
            public final TrackedValue<Boolean> customTranslatable = value(true);
        }
    }

    public static void init() {
        // Static initialization
        INSTANCE.special.overridenValue.setOverride(8);
    }
}

package com.lx862.qomc.config;

import com.lx862.qomc.xplat.Platform;
import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.*;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.NamingSchemes;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning.Type;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;

@DisplayName("Demo/Example Config")
@SerializedNameConvention(NamingSchemes.SNAKE_CASE)
public class DemoConfig extends ReflectiveConfig {
    private static final DemoConfig INSTANCE = DemoConfig.createToml(Platform.INSTANCE.getConfigPath(), "qomc", "demo", DemoConfig.class);

    @Comment("Demonstration of the different object type supported by QoMC")
    public final FieldType fieldType = new FieldType();

    @Comment("Demonstration of more special/specific pattern/fields")
    public final Special special = new Special();

    public static final class FieldType extends Section {
        @Comment("A boolean value. Possible value is true/false.")
        public final TrackedValue<Boolean> boolValue = value(false);

        @Comment("An integer value. Must be higher than -5000")
        @IntegerRange(min = -5000, max = Integer.MAX_VALUE)
        public final TrackedValue<Integer> integerValue = value(0);

        @Comment("A long value, positive value only.")
        @IntegerRange(min = 0, max = Long.MAX_VALUE)
        public final TrackedValue<Long> longValue = value(1L);

        @Comment("A double value. Range is limited from -100 to 100")
        @FloatRange(min = -100, max = 100)
        public final TrackedValue<Double> doubleValue = value(1d);

        @Comment("A float value. Range is limited from 0 to 100.")
        @FloatRange(min = 0, max = 100)
        public final TrackedValue<Float> floatValue = value(1f);

        @Comment("A string value.")
        public final TrackedValue<String> stringValue = value("Hello World");

        @Comment("An enum value. Available options: [MOD, RESOURCE_PACKS, DATAPACK, SHADERS, PLUSH].")
        public final TrackedValue<ContentType> enumValue = value(ContentType.MOD);

        @Matches("#[0-9A-Fa-f]{6}")
        @Comment("A string value, with a @Matches annotation of \"#[0-9A-Fa-f]{6}\", which will be picked up by QoMC as a RGB HEX color.")
        public final TrackedValue<String> rgbColorValue = value("#00FFFF");

        @Matches("#[0-9A-Fa-f]{6}")
        @Comment("A ValueList containing a list of string. The ValueList have a @Matches annotation of \"#[0-9A-Fa-f]{6}\", which will be picked up by QoMC as a RGB HEX color.")
        public final TrackedValue<ValueList<String>> colorListValue = list("", "#00FF00", "#FFFF00");

        @Comment("A String-Enum map about various addon content type for Minecraft.")
        public final TrackedValue<ValueMap<ContentType>> enumMapValue = map(ContentType.MOD).put("QoMC", ContentType.MOD).put("Programmer Art", ContentType.RESOURCE_PACK).put("BSL Shaders", ContentType.SHADERS).put("Blahaj", ContentType.PLUSH).build();
    }

    public static final class Special extends Section {

        @Comment("This config value is overriden in code, and the value in the config will not be effective.")
        public final TrackedValue<Integer> overridenValue = value(4);

        @Comment("A list of values making use of the @ChangeWarning annotations, prompting a remark for the user.")
        @Comment("Oh and this is a nested section!")
        public final ChangeWarnings changeWarnings = new ChangeWarnings();

        @DisplayName("Custom Display Name")
        @Comment("The @DisplayName annotation is supported (In this instance, configured to \"Custom Display Name\").")
        @Comment("This will be reflected in config outputs, though the value name in config will still be used for command nodes.")
        public final TrackedValue<String> displayName = value("Hello World");
    }

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

    public static void init() {
        // Static initialization
        INSTANCE.special.overridenValue.setOverride(8);
    }

    public enum ContentType {
        MOD,
        RESOURCE_PACK,
        DATAPACK,
        SHADERS,
        PLUSH
    }
}

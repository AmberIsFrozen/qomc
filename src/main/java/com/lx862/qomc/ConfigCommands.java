package com.lx862.qomc;

import com.lx862.qomc.core.ConfigTree;
import com.lx862.qomc.core.ConfigSectionTree;
import com.lx862.qomc.core.ValueType;
import com.lx862.qomc.exception.ConfigFailException;
import com.lx862.qomc.util.ColorUtil;
import com.lx862.qomc.util.ComponentUtil;
import com.lx862.qomc.util.ModInfo;
import com.lx862.qomc.util.QconfUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.values.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigCommands {
    public static LiteralArgumentBuilder<CommandSourceStack> buildModNode(ModInfo modInfo, List<Config> configs, CommandDispatcher<CommandSourceStack> ctx) {
        LiteralArgumentBuilder<CommandSourceStack> rootNode = Commands.literal(modInfo.id() + "Config")
        .requires(source -> source.hasPermission(4));

        for(Config config : configs) {
            if(configs.size() == 1) { // Mod with single config
                for(LiteralArgumentBuilder<CommandSourceStack> valueNode : buildConfigNodes(config)) {
                    rootNode.then(valueNode);
                }
            } else {
                LiteralArgumentBuilder<CommandSourceStack> configNode = Commands.literal(config.id());
                for(LiteralArgumentBuilder<CommandSourceStack> valueNodes : buildConfigNodes(config)) {
                    configNode.then(valueNodes);
                }
                rootNode.then(configNode);
            }
        }

        return rootNode;
    }

    public static List<LiteralArgumentBuilder<CommandSourceStack>> buildConfigNodes(Config config) {
        ConfigTree configTree = ConfigTree.of(config);

        List<LiteralArgumentBuilder<CommandSourceStack>> nodes = new ArrayList<>();
        for(TrackedValue<?> field : configTree.rootSection().fields()) {
            nodes.add(buildFieldNode(config, field));
        }

        for(Map.Entry<ValueKey, ConfigSectionTree> section : configTree.rootSection().sections().entrySet()) {
            LiteralArgumentBuilder<CommandSourceStack> sectionNode = buildSectionNode(config, section.getValue());
            nodes.add(sectionNode);
        }
        return nodes;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSectionNode(Config config, ConfigSectionTree sectionTree) {
        LiteralArgumentBuilder<CommandSourceStack> sectionNode = Commands.literal("[" + QconfUtil.getSerializedName(sectionTree.node()) + "]")
        .executes(ctx -> printSection(ctx, config, sectionTree.node(), sectionTree));

        sectionTree.fields().forEach(field -> sectionNode.then(buildFieldNode(config, field)));
        sectionTree.sections().values().forEach(
            (subSection) -> sectionNode.then(buildSectionNode(config, subSection))
        );

        return sectionNode;
    }

    private static <T> LiteralArgumentBuilder<CommandSourceStack> buildFieldNode(Config config, TrackedValue<T> trackedValue) {
        LiteralArgumentBuilder<CommandSourceStack> fieldNode = Commands.literal(QconfUtil.getSerializedName(trackedValue))
        .executes(ctx -> printField(ctx, config, trackedValue));

        ValueType valueType = ValueType.getType(trackedValue);
        List<ArgumentBuilder<CommandSourceStack, ?>> setValueNodes = buildSetValueNodes(trackedValue, valueType, (ctx, newValue) -> {
            if(valueType == ValueType.BOOLEAN || valueType == ValueType.STRING || valueType == ValueType.INTEGER || valueType == ValueType.LONG || valueType == ValueType.FLOAT || valueType == ValueType.DOUBLE) {
                return configSetValue(ctx, trackedValue, valueType, newValue);
            }
            if(valueType == ValueType.COLOR_RGB) {
                return configSetColorHex(ctx, (TrackedValue<String>) trackedValue, (String)newValue, false);
            }
            if(valueType == ValueType.COLOR_ARGB) {
                return configSetColorHex(ctx, (TrackedValue<String>) trackedValue, (String)newValue, true);
            }
            if(valueType == ValueType.ENUM) {
                return configSetEnum(ctx, (TrackedValue<Enum<?>>) trackedValue, (String)newValue);
            }
            return 0;
        });

        for(ArgumentBuilder<CommandSourceStack, ?> valueNode : setValueNodes) {
            fieldNode.then(valueNode);
        }

        // Node to reset to default value
        fieldNode.then(
            Commands.literal("{default}")
                .executes(ctx -> configSetValue(ctx, trackedValue, valueType, trackedValue.getDefaultValue()))
        );

        return fieldNode;
    }

    @FunctionalInterface
    private interface ValueSetCallback<T> {
        int onSet(CommandContext<CommandSourceStack> ctx, T value);
    }

    public static <T> List<ArgumentBuilder<CommandSourceStack, ?>> buildSetValueNodes(TrackedValue<T> trackedValue, ValueType valueType, ValueSetCallback<T> valueSetCallback) {
        List<ArgumentBuilder<CommandSourceStack, ?>> nodes = new ArrayList<>();

        // Probe for range constraint
        Constraint.Range<?> rangeConstraint = null;
        for (Constraint<?> constraint : trackedValue.constraints()) {
            if (constraint instanceof Constraint.Range<?>) rangeConstraint = (Constraint.Range<?>) constraint;
        }

        if(valueType == ValueType.BOOLEAN) {
            nodes.add(Commands.argument("boolean", BoolArgumentType.bool())
                    .executes(ctx -> valueSetCallback.onSet(ctx, (T)(Boolean)BoolArgumentType.getBool(ctx, "boolean")))
            );
        }
        if(valueType == ValueType.STRING) {
            nodes.add(Commands.argument("string", StringArgumentType.string())
                    .executes(ctx -> valueSetCallback.onSet(ctx, (T)StringArgumentType.getString(ctx, "string")))
            );
        }
        if(valueType == ValueType.COLOR_RGB) {
            nodes.add(Commands.argument("rgbColor", StringArgumentType.string())
                .executes(ctx -> valueSetCallback.onSet(ctx, (T)StringArgumentType.getString(ctx, "rgbColor")))
            );
        }
        if(valueType == ValueType.COLOR_ARGB) {
            nodes.add(Commands.argument("argbColor", StringArgumentType.string())
                .executes(ctx -> valueSetCallback.onSet(ctx, (T)StringArgumentType.getString(ctx, "argbColor")))
            );
        }
        if(valueType == ValueType.INTEGER) {
            IntegerArgumentType integerArgumentType = rangeConstraint == null ? IntegerArgumentType.integer() : IntegerArgumentType.integer((Integer) rangeConstraint.min(), (Integer) rangeConstraint.max());
            nodes.add(Commands.argument("number", integerArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, (T)(Integer)IntegerArgumentType.getInteger(ctx, "number")))
            );
        }
        if(valueType == ValueType.LONG) {
            LongArgumentType longArgumentType = rangeConstraint == null ? LongArgumentType.longArg() : LongArgumentType.longArg((Long) rangeConstraint.min(), (Long) rangeConstraint.max());
            nodes.add(Commands.argument("number", longArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, (T)(Long)LongArgumentType.getLong(ctx, "number")))
            );
        }
        if(valueType == ValueType.FLOAT) {
            FloatArgumentType floatArgumentType = rangeConstraint == null ? FloatArgumentType.floatArg() : FloatArgumentType.floatArg((Float) rangeConstraint.min(), (Float) rangeConstraint.max());
            nodes.add(Commands.argument("number", floatArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, (T)(Float)FloatArgumentType.getFloat(ctx, "number")))
            );
        }
        if(valueType == ValueType.DOUBLE) {
            DoubleArgumentType doubleArgumentType = rangeConstraint == null ? DoubleArgumentType.doubleArg() : DoubleArgumentType.doubleArg((Double) rangeConstraint.min(), (Double) rangeConstraint.max());
            nodes.add(Commands.argument("number", doubleArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, (T)(Double)DoubleArgumentType.getDouble(ctx, "number")))
            );
        }
        if(valueType == ValueType.ENUM) {
            final Object enumDefaultValue;
            if(trackedValue.value() instanceof ValueList<?>) {
                enumDefaultValue = ((ValueList<?>)trackedValue.value()).getDefaultValue(); // Have to infer from the list's default value
            } else if(trackedValue.value() instanceof ValueMap<?>) {
                enumDefaultValue = ((ValueMap<?>)trackedValue.value()).getDefaultValue(); // Have to infer from the map's default value
            } else {
                enumDefaultValue = trackedValue.getDefaultValue();
            }

            Enum<?>[] enumValues = (Enum[])enumDefaultValue.getClass().getEnumConstants();
            for(Enum<?> enumValue : enumValues) {
                String enumName = enumValue.name();
                nodes.add(
                    Commands.literal(enumName)
                            .executes(ctx -> valueSetCallback.onSet(ctx, (T)enumName))
                );
            }
        }

        if(valueType == ValueType.LIST) {
            TrackedValue<ValueList<Object>> list = (TrackedValue<ValueList<Object>>)trackedValue;
            ValueType childType = ValueType.getChildType(trackedValue, list.value().getDefaultValue());

            LiteralArgumentBuilder<CommandSourceStack> addNode = Commands.literal("add");
            for(ArgumentBuilder<CommandSourceStack, ?> addValueNode : buildSetValueNodes(trackedValue, childType, (ctx, newValue) -> configAddList(ctx, list, newValue))) {
                addNode.then(addValueNode);
            }

            LiteralArgumentBuilder<CommandSourceStack> removeNode = Commands.literal("remove");
            for(ArgumentBuilder<CommandSourceStack, ?> removeValueNode : buildSetValueNodes(trackedValue, childType, (ctx, newValue) -> configRemoveList(ctx, list, newValue))) {
                if(removeValueNode instanceof RequiredArgumentBuilder<?, ?>) {
                    RequiredArgumentBuilder<?, ?> requiredArgumentBuilder = (RequiredArgumentBuilder<?, ?>)removeValueNode;
                    requiredArgumentBuilder.suggests((commandContext, suggestionsBuilder) -> {
                        for(Object item : list.value()) {
                            String str = StringArgumentType.escapeIfRequired(item.toString());
                            suggestionsBuilder.suggest(str);
                        }
                        return suggestionsBuilder.buildFuture();
                    });
                }
                removeNode.then(removeValueNode);
            }

            nodes.add(addNode);
            nodes.add(removeNode);
        }

        if(valueType == ValueType.MAP) {
            TrackedValue<ValueMap<Object>> map = (TrackedValue<ValueMap<Object>>)trackedValue;
            ValueType childType = ValueType.getChildType(map, map.value().getDefaultValue());

            RequiredArgumentBuilder<CommandSourceStack, ?> setKeyNode = Commands.argument("key", StringArgumentType.string())
            .suggests((commandContext, suggestionsBuilder) -> {
                map.value().keySet().forEach(key -> suggestionsBuilder.suggest(StringArgumentType.escapeIfRequired(key)));
                return suggestionsBuilder.buildFuture();
            });
            for(ArgumentBuilder<CommandSourceStack, ?> mapNode : buildSetValueNodes(trackedValue, childType, (ctx, newValue) -> configSetMap(ctx, map, StringArgumentType.getString(ctx, "key"), newValue))) {
                setKeyNode.then(mapNode);
            }

            RequiredArgumentBuilder<CommandSourceStack, ?> removeKeyNode = Commands.argument("key", StringArgumentType.string())
                .suggests((commandContext, suggestionsBuilder) -> {
                    map.value().keySet().forEach(key -> suggestionsBuilder.suggest(StringArgumentType.escapeIfRequired(key)));
                    return suggestionsBuilder.buildFuture();
                })
                .executes(ctx -> configRemoveMap(ctx, map, StringArgumentType.getString(ctx, "key")));

            LiteralArgumentBuilder<CommandSourceStack> setNode = Commands.literal("set").then(setKeyNode);
            LiteralArgumentBuilder<CommandSourceStack> removeNode = Commands.literal("remove").then(removeKeyNode);
            nodes.add(setNode);
            nodes.add(removeNode);
        }

        return nodes;
    }

    private static int printField(CommandContext<CommandSourceStack> ctx, Config config, TrackedValue<?> trackedValue) {
        Platform.sendFeedback(ctx.getSource(), () -> ComponentUtil.configNodeBreadcrumb(config, trackedValue), false);
        Platform.sendFeedbacks(ctx.getSource(), ComponentUtil.configNodeComments(trackedValue), false);

        Platform.sendFeedback(ctx.getSource(), Component::empty, false);
        Platform.sendFeedback(ctx.getSource(), () -> ComponentUtil.currentValue(trackedValue)
                .withStyle(s -> s.withHoverEvent(Platform.hoverEventText(ComponentUtil.valueType(trackedValue)))), false);
        Platform.sendFeedback(ctx.getSource(), Component::empty, false);

        if(trackedValue.hasMetadata(ChangeWarning.TYPE)) {
            Platform.sendFeedback(ctx.getSource(), () -> ComponentUtil.configNodeChangeWarning(trackedValue.metadata(ChangeWarning.TYPE)), false);
            Platform.sendFeedback(ctx.getSource(), Component::empty, false);
        }

        MutableComponent changeText;

        if(trackedValue.isBeingOverridden()) {
            changeText = Component.literal("Value is overriden to " + QconfUtil.stringify(trackedValue.value()) + " by one or more mods.").withStyle(ChatFormatting.YELLOW);
        } else {
            changeText = Component.literal("[\uD83D\uDD8A Change]").withStyle(
                    Style.EMPTY
                            .withColor(ChatFormatting.GOLD)
                            .withUnderlined(true)
                            .withClickEvent(Platform.clickEventSuggestCommand(commandToString(ctx))));
        }

        Platform.sendFeedback(ctx.getSource(), () -> changeText, false);
        Platform.sendFeedback(ctx.getSource(), Component::empty, false);
        return 1;
    }

    private static int printSection(CommandContext<CommandSourceStack> ctx, Config config, ValueTreeNode configSection, ConfigSectionTree sectionTree) {
        Platform.sendFeedback(ctx.getSource(), () -> ComponentUtil.configNodeBreadcrumb(config, configSection), false);
        Platform.sendFeedbacks(ctx.getSource(), ComponentUtil.configNodeComments(configSection), false);
        Platform.sendFeedback(ctx.getSource(), Component::empty, false);

        if(configSection.hasMetadata(ChangeWarning.TYPE)) {
            Platform.sendFeedback(ctx.getSource(), () -> ComponentUtil.configNodeChangeWarning(configSection.metadata(ChangeWarning.TYPE)), false);
            Platform.sendFeedback(ctx.getSource(), Component::empty, false);
        }

        for(TrackedValue<?> trackedValue : sectionTree.fields()) {
            String command = commandToString(ctx) + QconfUtil.getSerializedName(trackedValue);

            Platform.sendFeedback(ctx.getSource(), () -> ComponentUtil.valueOverview(trackedValue)
                    .withStyle(s ->
                            s.withHoverEvent(Platform.hoverEventText(ComponentUtil.configNodeTooltip(trackedValue)))
                                    .withClickEvent(Platform.clickEventSuggestCommand(command))
                    ), false);
        }

        Platform.sendFeedback(ctx.getSource(), Component::empty, false);
        return 1;
    }

    private static <T> int configSetValue(CommandContext<CommandSourceStack> ctx, TrackedValue<T> trackedValue, ValueType valueType, T newValue) {
        try {
            setValue(trackedValue, newValue);
            Platform.sendFeedback(ctx.getSource(), () -> ComponentUtil.configFeedback(trackedValue, valueType), false);
            return 1;
        } catch (ConfigFailException exception) {
            Platform.sendFailure(ctx.getSource(), exception.component());
            return 0;
        }
    }

    private static int configSetColorHex(CommandContext<CommandSourceStack> ctx, TrackedValue<String> trackedValue, String input, boolean isARGB) {

        try {
            return configSetValue(ctx, trackedValue, isARGB ? ValueType.COLOR_ARGB : ValueType.COLOR_RGB, ColorUtil.colorToHex(ColorUtil.toArgbColor(input, isARGB), isARGB));
        } catch (NumberFormatException e) {
            Platform.sendFailure(ctx.getSource(), Component.literal("Invalid RGB Hex color format: " + input).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int configSetEnum(CommandContext<CommandSourceStack> ctx, TrackedValue<Enum<?>> value, String enumName) {
        return configSetValue(ctx, value, ValueType.ENUM, Enum.valueOf(value.getDefaultValue().getClass(), enumName));
    }

    private static <T> int configAddList(CommandContext<CommandSourceStack> ctx, TrackedValue<ValueList<T>> value, T item) {
        ValueList<T> newList = (ValueList<T>) value.value().copy();
        newList.add(item);
        try {
            setValue(value, newList);
            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("Added " + QconfUtil.stringify(item) + " to list " + QconfUtil.getDisplayName(value) + ".").withStyle(ChatFormatting.GREEN), false);
            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("New list: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, ValueType.LIST)), false);
            return 1;
        } catch (ConfigFailException exception) {
            Platform.sendFailure(ctx.getSource(), exception.component());
            return 0;
        }
    }

    private static <T> int configRemoveList(CommandContext<CommandSourceStack> ctx, TrackedValue<ValueList<T>> value, T item) {
        if(!value.value().contains(item)) {
            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("Value \"" + item + "\" is not in list " + QconfUtil.getDisplayName(value)).withStyle(ChatFormatting.RED), false);
            return 0;
        }
        value.value().remove(item);

        try {
            setValue(value, value.value());

            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("Removed " + QconfUtil.stringify(item) + " from list " + QconfUtil.getDisplayName(value) + ".").withStyle(ChatFormatting.GREEN), false);
            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("New list: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, ValueType.LIST)), false);
            return 1;
        } catch (ConfigFailException exception) {
            Platform.sendFailure(ctx.getSource(), exception.component());
            return 0;
        }
    }

    private static <T> int configSetMap(CommandContext<CommandSourceStack> ctx, TrackedValue<ValueMap<T>> value, String key, T item) {
        ValueMap<T> newMap = (ValueMap<T>) value.value().copy();
        newMap.put(key, item);

        try {
            setValue(value, newMap);

            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("Set " + key + " to " + QconfUtil.stringify(item) + ".").withStyle(ChatFormatting.GREEN), false);
            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("New map: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, ValueType.MAP)), false);
            return 1;
        } catch (ConfigFailException exception) {
            Platform.sendFeedback(ctx.getSource(), exception::component, false);
            return 0;
        }
    }

    private static <T> int configRemoveMap(CommandContext<CommandSourceStack> ctx, TrackedValue<ValueMap<T>> value, String key) {
        if(!value.value().containsKey(key)) {
            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("Value \"" + key + "\" is not in map " + QconfUtil.getDisplayName(value)).withStyle(ChatFormatting.RED), false);
            return 0;
        }
        value.value().remove(key);

        try {
            setValue(value, value.value());

            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("Removed " + key + " from map " + QconfUtil.getDisplayName(value) + ".").withStyle(ChatFormatting.GREEN), false);
            Platform.sendFeedback(ctx.getSource(), () -> Component.literal("New map: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, ValueType.MAP)), false);
            return 1;
        } catch (ConfigFailException exception) {
            Platform.sendFeedback(ctx.getSource(), exception::component, false);
            return 0;
        }
    }

    private static <T> void testConstraints(TrackedValue<T> trackedValue, T newValue) throws ConfigFailException {
        MutableComponent text = Component.literal("New value does not meet constraint(s): ");
        AtomicBoolean constraintFailed = new AtomicBoolean(false);
        trackedValue.checkForFailingConstraints(newValue).ifPresent(errorMessages -> {
            errorMessages.forEach(errMsg -> {
                text.append(Component.literal("\n- " + errMsg));
            });
            constraintFailed.set(true);
        });
        if(constraintFailed.get()) {
            throw new ConfigFailException(text);
        }
    }

    private static <T> void setValue(TrackedValue<T> trackedValue, T newValue) throws ConfigFailException {
        testConstraints(trackedValue, newValue);
        trackedValue.setValue(newValue);
    }

    private static String commandToString(CommandContext<CommandSourceStack> ctx) {
        String suggestedCommand = "/";
        for(ParsedCommandNode<CommandSourceStack> node : ctx.getNodes()) {
            suggestedCommand += node.getNode().getName() + " ";
        }
        return suggestedCommand;
    }
}

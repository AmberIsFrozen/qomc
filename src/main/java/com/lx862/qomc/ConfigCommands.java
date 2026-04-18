package com.lx862.qomc;

import com.lx862.qomc.command.AbstractCommandManager;
import com.lx862.qomc.command.AbstractCommandSource;
import com.lx862.qomc.core.CommandFeedback;
import com.lx862.qomc.core.ConfigTree;
import com.lx862.qomc.core.ConfigSectionTree;
import com.lx862.qomc.core.ValueType;
import com.lx862.qomc.exception.ConfigFailException;
import com.lx862.qomc.util.*;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lx862.qomc.util.VersionUtil.literalText;

public class ConfigCommands {
    public static <S> LiteralArgumentBuilder<S> buildModNode(AbstractCommandManager<S> commandManager, ModInfo modInfo, String commandLiteral, List<Config> configs) {
        LiteralArgumentBuilder<S> rootNode = commandManager.requirePermission(commandManager.literal(commandLiteral));

        for(Config config : configs) {
            ConfigTree configTree = ConfigTree.of(config);

            if(configs.size() == 1) { // Mod with single config
                rootNode.executes(ctx -> printConfig(ctx, commandManager.getCommandSource(ctx), config, configTree, modInfo));
                for(LiteralArgumentBuilder<S> valueNode : buildConfigNodes(commandManager, config, configTree)) {
                    rootNode.then(valueNode);
                }
            } else {
                LiteralArgumentBuilder<S> configNode = commandManager.literal(config.id());
                configNode.executes(ctx -> printConfig(ctx, commandManager.getCommandSource(ctx), config, configTree, modInfo));
                for(LiteralArgumentBuilder<S> valueNodes : buildConfigNodes(commandManager, config, configTree)) {
                    configNode.then(valueNodes);
                }
                rootNode.then(configNode);
            }
        }

        return rootNode;
    }

    public static <S> List<LiteralArgumentBuilder<S>> buildConfigNodes(AbstractCommandManager<S> commandManager, Config config, ConfigTree configTree) {
        List<LiteralArgumentBuilder<S>> nodes = new ArrayList<>();
        for(TrackedValue<?> field : configTree.rootSection().fields()) {
            nodes.add(buildFieldNode(commandManager, config, field));
        }

        for(Map.Entry<ValueKey, ConfigSectionTree> section : configTree.rootSection().sections().entrySet()) {
            LiteralArgumentBuilder<S> sectionNode = buildSectionNode(commandManager, config, section.getValue());
            nodes.add(sectionNode);
        }
        return nodes;
    }

    private static <S> LiteralArgumentBuilder<S> buildSectionNode(AbstractCommandManager<S> commandManager, Config config, ConfigSectionTree sectionTree) {
        LiteralArgumentBuilder<S> sectionNode = commandManager.literal("[" + QconfUtil.getSerializedName(sectionTree.node()) + "]")
        .executes(ctx -> printSection(ctx, commandManager.getCommandSource(ctx), config, sectionTree.node(), sectionTree));

        sectionTree.fields().forEach(field -> sectionNode.then(buildFieldNode(commandManager, config, field)));
        sectionTree.sections().values().forEach((subSection) -> sectionNode.then(buildSectionNode(commandManager, config, subSection)));

        return sectionNode;
    }

    private static <T, S> LiteralArgumentBuilder<S> buildFieldNode(AbstractCommandManager<S> commandManager, Config config, TrackedValue<T> trackedValue) {
        ValueType valueType = ValueType.getType(trackedValue, trackedValue.getDefaultValue());

        LiteralArgumentBuilder<S> fieldNode = commandManager.literal(QconfUtil.getSerializedName(trackedValue))
        .executes(ctx -> printField(ctx, commandManager.getCommandSource(ctx), config, trackedValue, valueType));

        List<ArgumentBuilder<S, ?>> setValueNodes = buildSetValueNodes(commandManager, trackedValue, valueType, (ctx, commandSource, newValue) -> {
            if(valueType == ValueType.BOOLEAN || valueType == ValueType.STRING || valueType == ValueType.INTEGER || valueType == ValueType.LONG || valueType == ValueType.FLOAT || valueType == ValueType.DOUBLE) {
                return configSetValue(commandSource, trackedValue, valueType, newValue);
            }
            if(valueType == ValueType.COLOR_RGB) {
                return configSetColorHex(commandSource, (TrackedValue<String>) trackedValue, (String)newValue, false);
            }
            if(valueType == ValueType.COLOR_ARGB) {
                return configSetColorHex(commandSource, (TrackedValue<String>) trackedValue, (String)newValue, true);
            }
            if(valueType == ValueType.ENUM) {
                return configSetEnum(commandSource, (TrackedValue<Enum<?>>) trackedValue, (Enum<?>)newValue);
            }
            return 0;
        });

        for(ArgumentBuilder<S, ?> valueNode : setValueNodes) {
            fieldNode.then(valueNode);
        }

        // Node to reset to default value
        fieldNode.then(
            commandManager.literal("{default}")
                .executes(ctx -> configSetValue(commandManager.getCommandSource(ctx), trackedValue, valueType, trackedValue.getDefaultValue()))
        );

        return fieldNode;
    }

    @FunctionalInterface
    protected interface ValueSetCallback<T, S> {
        int onSet(CommandContext<S> ctx, AbstractCommandSource commandSource, T value);
    }

    public static <T, S> List<ArgumentBuilder<S, ?>> buildSetValueNodes(AbstractCommandManager<S> commandManager, TrackedValue<T> trackedValue, ValueType valueType, ValueSetCallback<T, S> valueSetCallback) {
        List<ArgumentBuilder<S, ?>> nodes = new ArrayList<>();

        // Probe for range constraint
        Constraint.Range<?> rangeConstraint = null;
        for (Constraint<?> constraint : trackedValue.constraints()) {
            if (constraint instanceof Constraint.Range<?>) rangeConstraint = (Constraint.Range<?>) constraint;
        }

        if(valueType == ValueType.BOOLEAN) {
            nodes.add(commandManager.argument("boolean", BoolArgumentType.bool())
                    .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)(Boolean)BoolArgumentType.getBool(ctx, "boolean")))
            );
        }
        if(valueType == ValueType.STRING) {
            nodes.add(commandManager.argument("string", StringArgumentType.string())
                    .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)StringArgumentType.getString(ctx, "string")))
            );
        }
        if(valueType == ValueType.COLOR_RGB) {
            nodes.add(commandManager.argument("rgbColor", StringArgumentType.string())
                .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)StringArgumentType.getString(ctx, "rgbColor")))
            );
        }
        if(valueType == ValueType.COLOR_ARGB) {
            nodes.add(commandManager.argument("argbColor", StringArgumentType.string())
                .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)StringArgumentType.getString(ctx, "argbColor")))
            );
        }
        if(valueType == ValueType.INTEGER) {
            IntegerArgumentType integerArgumentType = rangeConstraint == null ? IntegerArgumentType.integer() : IntegerArgumentType.integer((Integer) rangeConstraint.min(), (Integer) rangeConstraint.max());
            nodes.add(commandManager.argument("number", integerArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)(Integer)IntegerArgumentType.getInteger(ctx, "number")))
            );
        }
        if(valueType == ValueType.LONG) {
            LongArgumentType longArgumentType = rangeConstraint == null ? LongArgumentType.longArg() : LongArgumentType.longArg((Long) rangeConstraint.min(), (Long) rangeConstraint.max());
            nodes.add(commandManager.argument("number", longArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)(Long)LongArgumentType.getLong(ctx, "number")))
            );
        }
        if(valueType == ValueType.FLOAT) {
            FloatArgumentType floatArgumentType = rangeConstraint == null ? FloatArgumentType.floatArg() : FloatArgumentType.floatArg((Float) rangeConstraint.min(), (Float) rangeConstraint.max());
            nodes.add(commandManager.argument("number", floatArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)(Float)FloatArgumentType.getFloat(ctx, "number")))
            );
        }
        if(valueType == ValueType.DOUBLE) {
            DoubleArgumentType doubleArgumentType = rangeConstraint == null ? DoubleArgumentType.doubleArg() : DoubleArgumentType.doubleArg((Double) rangeConstraint.min(), (Double) rangeConstraint.max());
            nodes.add(commandManager.argument("number", doubleArgumentType)
                    .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)(Double)DoubleArgumentType.getDouble(ctx, "number")))
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
                    commandManager.literal(enumName)
                        .executes(ctx -> valueSetCallback.onSet(ctx, commandManager.getCommandSource(ctx), (T)enumValue))
                );
            }
        }

        if(valueType == ValueType.LIST) {
            TrackedValue<ValueList<Object>> list = (TrackedValue<ValueList<Object>>)trackedValue;
            ValueType childType = ValueType.getType(trackedValue, list.value().getDefaultValue());

            LiteralArgumentBuilder<S> addNode = commandManager.literal("add");
            for(ArgumentBuilder<S, ?> addValueNode : buildSetValueNodes(commandManager, trackedValue, childType, (ctx, commandSource, newValue) -> configAddList(commandSource, list, newValue, childType))) {
                addNode.then(addValueNode);
            }

            LiteralArgumentBuilder<S> removeNode = commandManager.literal("remove");
            for(ArgumentBuilder<S, ?> removeValueNode : buildSetValueNodes(commandManager, trackedValue, childType, (ctx, commandSource, newValue) -> configRemoveList(commandSource, list, newValue, childType))) {
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
            ValueType childType = ValueType.getType(map, map.value().getDefaultValue());

            RequiredArgumentBuilder<S, ?> setKeyNode = commandManager.argument("key", StringArgumentType.string())
            .suggests((commandContext, suggestionsBuilder) -> {
                map.value().keySet().forEach(key -> suggestionsBuilder.suggest(StringArgumentType.escapeIfRequired(key)));
                return suggestionsBuilder.buildFuture();
            });
            for(ArgumentBuilder<S, ?> mapNode : buildSetValueNodes(commandManager, trackedValue, childType, (ctx, commandSource, newValue) -> configSetMap(commandSource, map, StringArgumentType.getString(ctx, "key"), newValue, childType))) {
                setKeyNode.then(mapNode);
            }

            RequiredArgumentBuilder<S, ?> removeKeyNode = commandManager.argument("key", StringArgumentType.string())
                .suggests((commandContext, suggestionsBuilder) -> {
                    map.value().keySet().forEach(key -> suggestionsBuilder.suggest(StringArgumentType.escapeIfRequired(key)));
                    return suggestionsBuilder.buildFuture();
                })
                .executes(ctx -> configRemoveMap(commandManager.getCommandSource(ctx), map, StringArgumentType.getString(ctx, "key"), childType));

            LiteralArgumentBuilder<S> setNode = commandManager.literal("set").then(setKeyNode);
            LiteralArgumentBuilder<S> removeNode = commandManager.literal("remove").then(removeKeyNode);
            nodes.add(setNode);
            nodes.add(removeNode);
        }

        return nodes;
    }

    private static int printConfig(CommandContext<?> ctx, AbstractCommandSource commandSource, Config config, ConfigTree configTree, ModInfo modInfo) {
        CommandFeedback feedback = new CommandFeedback();

        feedback.addEmptyLine();
        if(modInfo != null) {
            feedback.add(literalText(modInfo.name()).withStyle(ChatFormatting.BOLD).append(" ").append(literalText(modInfo.version()).withStyle(s -> s.withBold(false).withColor(ChatFormatting.GRAY))));
        }
        String configName = QconfUtil.getDisplayName(config, config.id());
        MutableComponent configLocation = literalText("(" + QconfUtil.getShortPath(config) + ")").withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(false));
        feedback.add(literalText("Config: ").withStyle(ChatFormatting.BOLD).append(literalText(configName).withStyle(s -> s.withBold(false))).append(" ").append(configLocation));
        feedback.add(literalText("--------------------------").withStyle(ChatFormatting.GRAY));

        printConfigInternal(ctx, feedback, config, configTree.rootSection(), -1);
        feedback.addEmptyLine();
        feedback.send(commandSource, false);
        return 1;
    }

    private static void printConfigInternal(CommandContext<?> ctx, CommandFeedback feedback, Config config, ConfigSectionTree sectionTree, int nestedLevel) {
        String indentStr = "";
        for(int i = 0; i < nestedLevel; i++) {
            indentStr += "  ";
        }

        if(sectionTree.node() != null) { // Root node would be null
            MutableComponent sectionText = literalText("[" + QconfUtil.getSerializedName(sectionTree.node()) + "]")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
            sectionText.withStyle(s -> s.withHoverEvent(VersionUtil.hoverEventText(ComponentUtil.configNodeTooltip(sectionTree.node()))));

            feedback.addEmptyLine();
            feedback.add(literalText(indentStr).append(sectionText));
        }

        for(TrackedValue<?> trackedValue : sectionTree.fields()) {
            ValueType valueType = ValueType.getType(trackedValue, trackedValue.getDefaultValue());
            StringBuilder command = new StringBuilder(commandToString(ctx));

            for(int i = 0; i < trackedValue.key().length(); i++) {
                ValueKey key = QconfUtil.trimKey(trackedValue.key(), trackedValue.key().length() - 1 - i);
                ValueTreeNode valueTreeNode = config.getNode(key);
                String nodeName = valueTreeNode instanceof ValueTreeNode.Section ? "[" + QconfUtil.getSerializedName(valueTreeNode) + "]" : QconfUtil.getSerializedName(valueTreeNode);
                command.append(nodeName).append(" ");
            }

            feedback.add(
                literalText(indentStr).append(ComponentUtil.valueOverview(trackedValue, valueType)
                .withStyle(s ->
                        s.withHoverEvent(VersionUtil.hoverEventText(ComponentUtil.configNodeTooltip(trackedValue)))
                                .withClickEvent(VersionUtil.clickEventSuggestCommand(command.toString()))
                )
            ));
        }

        for(ConfigSectionTree subSection : sectionTree.sections().values()) {
            printConfigInternal(ctx, feedback, config, subSection, nestedLevel+1);
        }
    }

    private static int printField(CommandContext<?> ctx, AbstractCommandSource commandSource, Config config, TrackedValue<?> trackedValue, ValueType valueType) {
        CommandFeedback feedback = new CommandFeedback();
        feedback.add(ComponentUtil.configNodeBreadcrumb(config, trackedValue));
        feedback.add(ComponentUtil.configNodeComments(trackedValue));

        feedback.addEmptyLine();
        feedback.add(ComponentUtil.currentValue(trackedValue, valueType)
                .withStyle(s -> s.withHoverEvent(
                        VersionUtil.hoverEventText(
                                ComponentUtil.valueType(valueType)
                                .append(ComponentUtil.constraints(trackedValue.constraints()))
                        )
                    )
                )
        );
        feedback.addEmptyLine();

        if(trackedValue.hasMetadata(ChangeWarning.TYPE)) {
            feedback.add(ComponentUtil.configNodeChangeWarning(trackedValue.metadata(ChangeWarning.TYPE)));
            feedback.addEmptyLine();
        }

        MutableComponent changeText = literalText("[✎ Change]").withStyle(
            Style.EMPTY
                    .withColor(ChatFormatting.GOLD)
                    .withUnderlined(true)
                    .withClickEvent(VersionUtil.clickEventSuggestCommand(commandToString(ctx)))
        );

        feedback.add(changeText);
        feedback.addEmptyLine();

        feedback.send(commandSource, false);
        return 1;
    }

    private static int printSection(CommandContext<?> ctx, AbstractCommandSource commandSource, Config config, ValueTreeNode configSection, ConfigSectionTree sectionTree) {
        CommandFeedback feedback = new CommandFeedback();

        feedback.add(ComponentUtil.configNodeBreadcrumb(config, configSection));
        feedback.add(ComponentUtil.configNodeComments(configSection));
        feedback.addEmptyLine();

        if(configSection.hasMetadata(ChangeWarning.TYPE)) {
            feedback.add(ComponentUtil.configNodeChangeWarning(configSection.metadata(ChangeWarning.TYPE)));
            feedback.addEmptyLine();
        }

        for(TrackedValue<?> trackedValue : sectionTree.fields()) {
            ValueType valueType = ValueType.getType(trackedValue, trackedValue.getDefaultValue());
            String command = commandToString(ctx) + QconfUtil.getSerializedName(trackedValue);

            feedback.add(
                    ComponentUtil.valueOverview(trackedValue, valueType)
                    .withStyle(s ->
                            s.withHoverEvent(VersionUtil.hoverEventText(ComponentUtil.configNodeTooltip(trackedValue)))
                                    .withClickEvent(VersionUtil.clickEventSuggestCommand(command))
                    )
            );
        }

        feedback.addEmptyLine();

        feedback.send(commandSource, false);
        return 1;
    }

    private static <T> int configSetValue(AbstractCommandSource commandSource, TrackedValue<T> trackedValue, ValueType valueType, T newValue) {
        try {
            setValue(trackedValue, newValue);
            commandSource.sendFeedback(ComponentUtil.configFeedback(trackedValue, valueType), false);
            return 1;
        } catch (ConfigFailException exception) {
            commandSource.sendFeedback(exception.component(), false);
            return 0;
        }
    }

    private static int configSetColorHex(AbstractCommandSource commandSource, TrackedValue<String> trackedValue, String input, boolean isARGB) {
        try {
            return configSetValue(commandSource, trackedValue, isARGB ? ValueType.COLOR_ARGB : ValueType.COLOR_RGB, ColorUtil.colorToHex(ColorUtil.toArgbColor(input, isARGB), isARGB));
        } catch (NumberFormatException e) {
            commandSource.sendFailure(literalText("Invalid RGB Hex color format: " + input).withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static int configSetEnum(AbstractCommandSource commandSource, TrackedValue<Enum<?>> value, Enum<?> enumName) {
        return configSetValue(commandSource, value, ValueType.ENUM, enumName);
    }

    private static <T> int configAddList(AbstractCommandSource commandSource, TrackedValue<ValueList<T>> value, T item, ValueType childType) {
        ValueList<T> newList = (ValueList<T>) value.value().copy();
        newList.add(item);
        try {
            setValue(value, newList);

            commandSource.sendFeedback(literalText("Inserted ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, item, childType)).append(literalText(" to list!")), false);
            commandSource.sendFeedback(literalText("New list: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, value.getRealValue(), ValueType.LIST)), false);
            return 1;
        } catch (ConfigFailException exception) {
            commandSource.sendFailure(exception.component());
            return 0;
        }
    }

    private static <T> int configRemoveList(AbstractCommandSource commandSource, TrackedValue<ValueList<T>> value, T item, ValueType childType) {
        if(!value.value().contains(item)) {
            commandSource.sendFeedback(literalText("Value \"" + item + "\" is not in list " + QconfUtil.getDisplayName(value)).withStyle(ChatFormatting.RED), false);
            return 0;
        }
        ValueList<T> newList = (ValueList<T>) value.value().copy();
        newList.remove(item);

        try {
            setValue(value, newList);

            commandSource.sendFeedback(literalText("Removed ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, item, childType)).append(literalText(" from list!")), false);
            commandSource.sendFeedback(literalText("New list: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, value.getRealValue(), ValueType.LIST)), false);
            return 1;
        } catch (ConfigFailException exception) {
            commandSource.sendFailure(exception.component());
            return 0;
        }
    }

    private static <T> int configSetMap(AbstractCommandSource commandSource, TrackedValue<ValueMap<T>> value, String key, T item, ValueType childType) {
        ValueMap<T> newMap = (ValueMap<T>) value.value().copy();
        newMap.put(key, item);

        try {
            setValue(value, newMap);

            commandSource.sendFeedback(literalText("\"" + key + "\" set to ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, item, childType)).append(" in map!"), false);
            commandSource.sendFeedback(literalText("New map: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, value.getRealValue(), ValueType.MAP)), false);
            return 1;
        } catch (ConfigFailException exception) {
            commandSource.sendFailure(exception.component());
            return 0;
        }
    }

    private static <T> int configRemoveMap(AbstractCommandSource commandSource, TrackedValue<ValueMap<T>> value, String key, ValueType childType) {
        if(!value.value().containsKey(key)) {
            commandSource.sendFeedback(literalText("Value \"" + key + "\" is not in the map!").withStyle(ChatFormatting.RED), false);
            return 0;
        }
        ValueMap<T> newMap = (ValueMap<T>)value.value().copy();
        T removedValue = newMap.remove(key);

        try {
            setValue(value, newMap);

            commandSource.sendFeedback(literalText("Removed \"" + key + "\" with value ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, removedValue, childType)).append(" from map!"), false);
            commandSource.sendFeedback(literalText("New map: ").withStyle(ChatFormatting.GREEN).append(ComponentUtil.formatValue(value, value.getRealValue(), ValueType.MAP)), false);
            return 1;
        } catch (ConfigFailException exception) {
            commandSource.sendFailure(exception.component());
            return 0;
        }
    }

    private static <T> void testConstraints(TrackedValue<T> trackedValue, T newValue) throws ConfigFailException {
        MutableComponent text = literalText("New value does not meet constraint(s): ");
        AtomicBoolean constraintFailed = new AtomicBoolean(false);
        trackedValue.checkForFailingConstraints(newValue).ifPresent(errorMessages -> {
            errorMessages.forEach(errMsg -> {
                text.append(literalText("\n- " + errMsg));
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

    private static String commandToString(CommandContext<?> ctx) {
        String suggestedCommand = "/";
        for(ParsedCommandNode<?> node : ctx.getNodes()) {
            suggestedCommand += node.getNode().getName() + " ";
        }
        return suggestedCommand;
    }
}

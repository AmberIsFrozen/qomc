package com.lx862.qomc;

import com.lx862.qomc.core.ConfigTree;
import com.lx862.qomc.core.ConfigSection;
import com.lx862.qomc.core.ValueType;
import com.lx862.qomc.util.ColorUtil;
import com.lx862.qomc.util.McUtil;
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
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.function.BiFunction;

public class QomcCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> buildModNode(ModMetadata modMetadata, List<Config> configs, CommandDispatcher<ServerCommandSource> ctx) {
        LiteralArgumentBuilder<ServerCommandSource> rootNode = CommandManager.literal(modMetadata.getId() + "Config");
        rootNode.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4));

        for(Config config : configs) {
            boolean singleConfigMod = configs.size() == 1;

            if(singleConfigMod) {
                for(LiteralArgumentBuilder<ServerCommandSource> node : buildConfigNodes(config)) {
                    rootNode.then(node);
                }
            } else {
                LiteralArgumentBuilder<ServerCommandSource> subConfigNode = CommandManager.literal(config.id());
                for(LiteralArgumentBuilder<ServerCommandSource> node : buildConfigNodes(config)) {
                    subConfigNode.then(node);
                }
                rootNode.then(subConfigNode);
            }
        }

        return rootNode;
    }

    public static List<LiteralArgumentBuilder<ServerCommandSource>> buildConfigNodes(Config config) {
        ConfigTree configTree = ConfigTree.of(config);

        List<LiteralArgumentBuilder<ServerCommandSource>> nodes = new ArrayList<>();
        for(TrackedValue<?> field : configTree.rootSection().fields()) {
            LiteralArgumentBuilder<ServerCommandSource> fieldNode = buildTrackedValueNode(config, field);
            nodes.add(fieldNode);
        }

        for(Map.Entry<ValueKey, ConfigSection> fieldEntriesEntry : configTree.rootSection().sections().entrySet()) {
            var section = buildSectionNode(config, fieldEntriesEntry.getKey(), fieldEntriesEntry.getValue());
            nodes.add(section);
        }
        return nodes;
    }

    public static LiteralArgumentBuilder<ServerCommandSource> buildSectionNode(Config config, ValueKey sectionKey, ConfigSection configSection) {
        LiteralArgumentBuilder<ServerCommandSource> sectionNode = CommandManager.literal("[" + sectionKey.getLastComponent() + "]");

        sectionNode.executes(ctx -> {
            ValueTreeNode sectionConfigNode = config.getNode(sectionKey);

            Platform.sendFeedback(ctx.getSource(), () -> McUtil.configNodeBreadcrumb(config, sectionConfigNode), false);
            Platform.sendFeedback(ctx.getSource(), () -> McUtil.configNodeComments(sectionConfigNode), false);
            Platform.sendFeedback(ctx.getSource(), () -> Text.empty(), false);

            if(sectionConfigNode.hasMetadata(ChangeWarning.TYPE)) {
                Platform.sendFeedback(ctx.getSource(), () -> McUtil.configNodeChangeWarning(sectionConfigNode.metadata(ChangeWarning.TYPE)), false);
                Platform.sendFeedback(ctx.getSource(), () -> Text.empty(), false);
            }

            for(TrackedValue<?> value : configSection.fields()) {
                Platform.sendFeedback(ctx.getSource(), () -> McUtil.valueOverview(value), false);
            }

            return 1;
        });

        for(TrackedValue<?> trackedValue : configSection.fields()) {
            LiteralArgumentBuilder<ServerCommandSource> trackedValueNode = buildTrackedValueNode(config, trackedValue);
            sectionNode.then(trackedValueNode);
        }
        for(Map.Entry<ValueKey, ConfigSection> section : configSection.sections().entrySet()) {
            LiteralArgumentBuilder<ServerCommandSource> nestedSectionNode = buildSectionNode(config, section.getKey(), section.getValue());
            sectionNode.then(nestedSectionNode);
        }
        return sectionNode;
    }


    public static <T> LiteralArgumentBuilder<ServerCommandSource> buildTrackedValueNode(Config config, TrackedValue<T> trackedValue) {
        LiteralArgumentBuilder<ServerCommandSource> fieldNode = CommandManager.literal(trackedValue.key().getLastComponent());
        fieldNode.executes(ctx -> {
            Platform.sendFeedback(ctx.getSource(), () -> McUtil.configNodeBreadcrumb(config, trackedValue), false);
            Platform.sendFeedback(ctx.getSource(), () -> McUtil.configNodeComments(trackedValue), false);

            Platform.sendFeedback(ctx.getSource(), () -> Text.empty(), false);
            Platform.sendFeedback(ctx.getSource(), () -> McUtil.currentValue(trackedValue), false);
            Platform.sendFeedback(ctx.getSource(), () -> Text.empty(), false);

            if(trackedValue.hasMetadata(ChangeWarning.TYPE)) {
                Platform.sendFeedback(ctx.getSource(), () -> McUtil.configNodeChangeWarning(trackedValue.metadata(ChangeWarning.TYPE)), false);
                Platform.sendFeedback(ctx.getSource(), () -> Text.empty(), false);
            }

            String suggestedCommand = "/";
            for(ParsedCommandNode<ServerCommandSource> node : ctx.getNodes()) {
                suggestedCommand += node.getNode().getName() + " ";
            }

            MutableText changeText = Text.literal("[\uD83D\uDD8A Change]").fillStyle(
                    Style.EMPTY
                            .withColor(Formatting.GOLD)
                            .withUnderline(true)
                            .withHoverEvent(Platform.hoverEventText(McUtil.valueType(trackedValue)))
                            .withClickEvent(Platform.clickEventSuggestCommand(suggestedCommand)));

            Platform.sendFeedback(ctx.getSource(), () -> changeText, false);
            return 1;
        });

        ValueType valueType = ValueType.fromValue(trackedValue);
        List<ArgumentBuilder<ServerCommandSource, ?>> valueNodes = buildValueNode(trackedValue, valueType, (ctx, newValue) -> {
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
                return configSetEnum(ctx, (TrackedValue<Enum<?>>)trackedValue, (String)newValue);
            }
            return 0;
        });

        for(ArgumentBuilder<ServerCommandSource, ?> valueNode : valueNodes) {
            fieldNode.then(valueNode);
        }

        fieldNode.then(
            CommandManager.literal("{default}")
                .executes(ctx -> configSetValue(ctx, trackedValue, valueType, trackedValue.getDefaultValue()))
        );

        return fieldNode;
    }

    public static <T> List<ArgumentBuilder<ServerCommandSource, ?>> buildValueNode(TrackedValue<T> value, ValueType valueType, BiFunction<CommandContext<ServerCommandSource>, T, Integer> callback) {
        Constraint.Range<?> rangeConstraint = null;
        for (Constraint<?> constraint : value.constraints()) {
            if (constraint instanceof Constraint.Range<?>) rangeConstraint = (Constraint.Range<?>) constraint;
        }
        List<ArgumentBuilder<ServerCommandSource, ?>> arguments = new ArrayList<>();

        if(valueType == ValueType.BOOLEAN) {
            arguments.add(CommandManager.argument("boolean", BoolArgumentType.bool())
                    .executes(ctx -> callback.apply(ctx, (T)(Boolean)BoolArgumentType.getBool(ctx, "boolean"))));
        }
        if(valueType == ValueType.STRING) {
            arguments.add(CommandManager.argument("string", StringArgumentType.greedyString())
                    .executes(ctx -> callback.apply(ctx, (T)StringArgumentType.getString(ctx, "string"))));
        }
        if(valueType == ValueType.COLOR_RGB) {
            arguments.add(CommandManager.argument("rgbColor", StringArgumentType.string())
                .executes(ctx -> callback.apply(ctx, (T)StringArgumentType.getString(ctx, "rgbColor")))
            );
        }
        if(valueType == ValueType.COLOR_ARGB) {
            arguments.add(CommandManager.argument("argbColor", StringArgumentType.string())
                .executes(ctx -> callback.apply(ctx, (T)StringArgumentType.getString(ctx, "argbColor")))
            );
        }
        if(valueType == ValueType.INTEGER) {
            IntegerArgumentType integerArgumentType = rangeConstraint == null ? IntegerArgumentType.integer() : IntegerArgumentType.integer((Integer) rangeConstraint.min(), (Integer) rangeConstraint.max());
            arguments.add(CommandManager.argument("number", integerArgumentType)
                    .executes(ctx -> callback.apply(ctx, (T)(Integer)IntegerArgumentType.getInteger(ctx, "number"))));
        }
        if(valueType == ValueType.LONG) {
            LongArgumentType longArgumentType = rangeConstraint == null ? LongArgumentType.longArg() : LongArgumentType.longArg((Long) rangeConstraint.min(), (Long) rangeConstraint.max());
            arguments.add(CommandManager.argument("number", longArgumentType)
                    .executes(ctx -> callback.apply(ctx, (T)(Long)LongArgumentType.getLong(ctx, "number"))));
        }
        if(valueType == ValueType.FLOAT) {
            FloatArgumentType floatArgumentType = rangeConstraint == null ? FloatArgumentType.floatArg() : FloatArgumentType.floatArg((Float) rangeConstraint.min(), (Float) rangeConstraint.max());
            arguments.add(CommandManager.argument("number", floatArgumentType)
                    .executes(ctx -> callback.apply(ctx, (T)(Float)FloatArgumentType.getFloat(ctx, "number"))));
        }
        if(valueType == ValueType.DOUBLE) {
            DoubleArgumentType doubleArgumentType = rangeConstraint == null ? DoubleArgumentType.doubleArg() : DoubleArgumentType.doubleArg((Double) rangeConstraint.min(), (Double) rangeConstraint.max());
            arguments.add(CommandManager.argument("number", doubleArgumentType)
                    .executes(ctx -> callback.apply(ctx, (T)(Double)DoubleArgumentType.getDouble(ctx, "number"))));
        }
        if(valueType == ValueType.ENUM) {
            Enum<?>[] enumValues = (Enum[])value.getDefaultValue().getClass().getEnumConstants();
            for(var enumValue : enumValues) {
                String enumName = enumValue.name();
                arguments.add(
                    CommandManager.literal(enumName)
                            .executes(ctx -> callback.apply(ctx, (T)enumName))
                );
            }
        }
        if(valueType == ValueType.LIST) {
            TrackedValue<ValueList<Object>> listTrackedValue = (TrackedValue<ValueList<Object>>)value;
            ValueType listContentValueType = ValueType.fromValue(value, listTrackedValue.value().getDefaultValue());
            LiteralArgumentBuilder<ServerCommandSource> addNode = CommandManager.literal("add");
            LiteralArgumentBuilder<ServerCommandSource> removeNode = CommandManager.literal("remove");

            for(ArgumentBuilder<ServerCommandSource, ?> listNode : buildValueNode(value, listContentValueType, (ctx, newValue) -> {
                return configAddList(ctx, listTrackedValue, newValue);
            })) {
                addNode.then(listNode);
            }

            for(ArgumentBuilder<ServerCommandSource, ?> listNode : buildValueNode(value, listContentValueType, (ctx, newValue) -> {
                return configRemoveList(ctx, listTrackedValue, newValue);
            })) {
                if(listNode instanceof RequiredArgumentBuilder<?, ?> requiredArgumentBuilder) {
                    requiredArgumentBuilder.suggests((commandContext, suggestionsBuilder) -> {
                        for(Object item : listTrackedValue.value()) {
                            String str = item.toString();
                            if(item instanceof String) {
                                str = "\"" + str + "\"";
                            }
                            suggestionsBuilder.suggest(str);
                        }
                        return suggestionsBuilder.buildFuture();
                    });
                }
                removeNode.then(listNode);
            }

            arguments.add(addNode);
            arguments.add(removeNode);
        }

        return arguments;
    }

    private static <T> int configSetValue(CommandContext<ServerCommandSource> ctx, TrackedValue<T> trackedValue, ValueType valueType, T newValue) {
        setValue(trackedValue, newValue);
        Platform.sendFeedback(ctx.getSource(), () -> McUtil.configFeedback(trackedValue, valueType), false);
        return 1;
    }

    private static int configSetColorHex(CommandContext<ServerCommandSource> ctx, TrackedValue<String> trackedValue, String input, boolean isARGB) {
        testConstraints(trackedValue, input);
        try {
            return configSetValue(ctx, trackedValue, isARGB ? ValueType.COLOR_ARGB : ValueType.COLOR_RGB, ColorUtil.colorToHex(ColorUtil.toArgbColor(input, isARGB), isARGB));
        } catch (NumberFormatException e) {
            throw new CommandException(Text.literal("Invalid RGB Hex color format: " + input));
        }
    }

    private static int configSetEnum(CommandContext<ServerCommandSource> ctx, TrackedValue<Enum<?>> value, String enumName) {
        return configSetValue(ctx, value, ValueType.ENUM, Enum.valueOf(value.getDefaultValue().getClass(), enumName));
    }

    private static <T> int configAddList(CommandContext<ServerCommandSource> ctx, TrackedValue<ValueList<T>> value, T item) {
        ValueList<T> newList = (ValueList<T>) value.value().copy();
        newList.add(item);
        testConstraints(value, newList);
        setValue(value, newList);
        Platform.sendFeedback(ctx.getSource(), () -> Text.literal("Added " + QconfUtil.stringify(item) + " to list " + QconfUtil.getDisplayOrDefaultName(value) + ".").formatted(Formatting.GREEN), false);
        Platform.sendFeedback(ctx.getSource(), () -> Text.literal("New list: ").formatted(Formatting.GREEN).append(McUtil.formatValue(value, ValueType.LIST)), false);
        return 1;
    }

    private static <T> int configRemoveList(CommandContext<ServerCommandSource> ctx, TrackedValue<ValueList<T>> value, T item) {
        if(!value.value().contains(item)) {
            Platform.sendFeedback(ctx.getSource(), () -> Text.literal("Value \"" + item + "\" is not in list " + QconfUtil.getDisplayOrDefaultName(value)).formatted(Formatting.RED), false);
            return 0;
        }

        value.value().remove(item);
        Platform.sendFeedback(ctx.getSource(), () -> Text.literal("Removed " + QconfUtil.stringify(item) + " from list " + QconfUtil.getDisplayOrDefaultName(value) + ".").formatted(Formatting.GREEN), false);
        Platform.sendFeedback(ctx.getSource(), () -> Text.literal("New list: ").formatted(Formatting.GREEN).append(McUtil.formatValue(value, ValueType.LIST)), false);
        setValue(value, value.value());
        return 1;
    }

    private static <T> void testConstraints(TrackedValue<T> trackedValue, T newValue) {
        trackedValue.checkForFailingConstraints(newValue).ifPresent(errorMessages -> {
            MutableText text = Text.literal("New value does not meet constraint(s): ");
            errorMessages.forEach(errMsg -> {
                text.append(Text.literal("\n- " + errMsg));
            });
            throw new CommandException(text);
        });
    }

    private static <T> void setValue(TrackedValue<T> trackedValue, T newValue) {
        testConstraints(trackedValue, newValue);
        trackedValue.setValue(newValue);
    }
}

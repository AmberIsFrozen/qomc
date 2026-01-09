package com.lx862.qomc.util;

import com.lx862.qomc.Platform;
import com.lx862.qomc.core.ValueType;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class McUtil {
    public static Component configFeedback(TrackedValue<?> value, ValueType valueType) {
        MutableComponent keyName = Component.literal(QconfUtil.getDisplayOrDefaultName(value)).withStyle(s -> s
                .applyFormat(ChatFormatting.GREEN)
                .withUnderlined(true));

        MutableComponent text = Component.literal(" has been set to ").append(McUtil.formatValue(value, valueType)).withStyle(Style.EMPTY.withUnderlined(false));
        return keyName.append(text);
    }

    public static MutableComponent configNodeBreadcrumb(Config config, ValueTreeNode node) {
        MutableComponent fieldHeader = Component.empty();

        for(int i = 0; i < node.key().length(); i++) {
            ValueTreeNode breadcrumbNode = config.getNode(QconfUtil.trimKey(node.key(), node.key().length()-1-i));
            MutableComponent nodeText;

            if(breadcrumbNode instanceof ValueTreeNode.Section) {
                nodeText = Component.literal("[" + breadcrumbNode.key().getLastComponent() + "]")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
            } else {
                nodeText = Component.literal(QconfUtil.getDisplayOrDefaultName(breadcrumbNode)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(true));
            }

            if(i != node.key().length()-1) { // Add hover text
                nodeText.withStyle(s -> s.withHoverEvent(Platform.hoverEventText(configNodeTooltip(breadcrumbNode))));
            }

            fieldHeader.append(nodeText);
            if(i != node.key().length()-1) {
                MutableComponent arrowText = Component.literal(" > ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
                fieldHeader.append(arrowText);
            }
        }
        return fieldHeader;
    }

    public static MutableComponent configNodeComments(ValueTreeNode node) {
        MutableComponent text = Component.empty().withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withBold(false));
        if(node.hasMetadata(Comment.TYPE)) {
            boolean prependNewLine = false;
            for(String comment : node.metadata(Comment.TYPE)) {
                text.append(Component.literal((prependNewLine ? "\n" : "") + comment));
                prependNewLine = true;
            }
        }
        return text;
    }

    public static MutableComponent configNodeTooltip(ValueTreeNode node) {
        if(node instanceof ValueTreeNode.Section) {
            return Component.literal(QconfUtil.getDisplayOrDefaultName(node)).withStyle(ChatFormatting.GREEN).append("\n").append(configNodeComments(node));
        } else {
            return Component.literal(QconfUtil.getDisplayOrDefaultName(node)).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD).append("\n").append(configNodeComments(node));
        }
    }

    public static MutableComponent valueOverview(TrackedValue<?> trackedValue) {
        MutableComponent text = Component.literal("- ").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
        MutableComponent valueName = Component.literal(trackedValue.key().getLastComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false));
        valueName.withStyle(s -> s.withHoverEvent(Platform.hoverEventText(configNodeTooltip(trackedValue))));

        MutableComponent t3 = Component.literal(": ").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false));
        MutableComponent t4 = formatValue(trackedValue, ValueType.fromValue(trackedValue)).withStyle(Style.EMPTY.withUnderlined(true).withBold(false));

        return text.append(valueName).append(t3).append(t4);
    }

    public static MutableComponent valueType(TrackedValue<?> trackedValue) {
        ValueType valueType = ValueType.fromValue(trackedValue);
        MutableComponent finalText = Component.literal("Type: " + valueType.name);

        for(Constraint<?> constraint : trackedValue.constraints()) {
            if(constraint instanceof Constraint.Range<?> rangeConstraint) {
                Object low = rangeConstraint.min();
                Object high = rangeConstraint.max();

                MutableComponent rangeConstraintText = Component.literal(" [" + low + "-" + high + "]").withStyle(ChatFormatting.YELLOW);
                finalText.append(rangeConstraintText);
            } else if(constraint instanceof Constraint.All<?>) {
                MutableComponent constraintText = Component.literal(" [" + constraint.getRepresentation() + "]").withStyle(ChatFormatting.YELLOW);
                finalText.append(constraintText);
            }
        }

        return finalText;
    }

    public static MutableComponent currentValue(TrackedValue<?> trackedValue) {
        boolean valueIsDefault = Objects.equals(trackedValue.value(), trackedValue.getDefaultValue());
        MutableComponent headerText = Component.literal("Current Value: ");
        MutableComponent valueText = formatValue(trackedValue, ValueType.fromValue(trackedValue)).withStyle(s -> s.withUnderlined(true));

        MutableComponent defaultText;

        if(valueIsDefault) {
            defaultText = Component.literal(" (Default)").withStyle(ChatFormatting.GRAY);
        } else {
            defaultText = Component.literal(" (Changed)")
                    .withStyle(s -> s.withHoverEvent(Platform.hoverEventText(Component.literal("Default: " + QconfUtil.stringify(trackedValue.getDefaultValue())).withStyle(ChatFormatting.GRAY))))
                    .withStyle(ChatFormatting.YELLOW);
        }
        return headerText.append(valueText).append(defaultText);
    }

    public static MutableComponent configNodeChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning changeWarning) {
        MutableComponent headerText = Component.literal("Note: ").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true));

        MutableComponent warningText =
            switch(changeWarning.getType()) {
                case Custom -> Component.literal(changeWarning.getCustomMessage());
                case CustomTranslatable -> Component.translatable(changeWarning.getCustomMessage());
                case RequiresRestart -> Component.literal("Restart is required for changes to apply");
                case Experimental -> Component.literal("Experimental option, use with caution!");
                case Unsafe -> Component.literal("Unsafe option, use at your own risk!");
            };

        warningText.withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(false));
        return Component.literal("\n").append(headerText).append(warningText);
    }

    public static MutableComponent formatValue(TrackedValue<?> trackedValue, ValueType valueType) {
        if(valueType == ValueType.LIST) {
            MutableComponent text = Component.literal("[").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE));
            ValueList<?> list = (ValueList<?>)trackedValue.value();
            ValueType listValueType = ValueType.fromValue(trackedValue, list.getDefaultValue());

            for(int i = 0; i < list.size(); i++) {
                Object innerValue = list.get(i);
                text.append(formatSimpleValue(innerValue, listValueType));
                if(i != list.size()-1) text.append(Component.literal(", ").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE)));
            }
            text.append("]").withStyle(s -> s.withUnderlined(false).applyFormat(ChatFormatting.WHITE));
            return text;
        }

        return formatSimpleValue(trackedValue.value(), valueType);
    }

    private static MutableComponent formatSimpleValue(Object obj, ValueType valueType) {
        String str = QconfUtil.stringify(obj);
        MutableComponent baseText = Component.literal(str);
        if(valueType == ValueType.BOOLEAN) {
            if(str.equals("true")) return baseText.withStyle(ChatFormatting.GREEN);
            else return baseText.withStyle(ChatFormatting.RED);
        } else if(valueType == ValueType.INTEGER || valueType == ValueType.LONG || valueType == ValueType.DOUBLE || valueType == ValueType.FLOAT) {
            return baseText.withStyle(ChatFormatting.GOLD);
        } else if(valueType == ValueType.STRING) {
            return baseText.withStyle(ChatFormatting.AQUA);
        } else if(valueType == ValueType.ENUM) {
            return baseText.withStyle(ChatFormatting.BLUE);
        } else if(valueType == ValueType.COLOR_RGB || valueType == ValueType.COLOR_ARGB) {
            boolean hasAlpha = valueType == ValueType.COLOR_ARGB;
            String unquotedString = str.substring(1, str.length()-1);
            ColorUtil.ArgbColor color = ColorUtil.toArgbColor(unquotedString, true);
            int rgb = color.pack();

            MutableComponent finalText = Component.literal("#").withStyle(Style.EMPTY.withColor(rgb));

            MutableComponent aPreview = Component.literal(ColorUtil.colorToHex(color.alpha()).toUpperCase(Locale.ROOT));
            MutableComponent rPreview = Component.literal(ColorUtil.colorToHex(color.red()).toUpperCase(Locale.ROOT));
            MutableComponent gPreview = Component.literal(ColorUtil.colorToHex(color.green()).toUpperCase(Locale.ROOT));
            MutableComponent bPreview = Component.literal(ColorUtil.colorToHex(color.blue()).toUpperCase(Locale.ROOT));

            if(hasAlpha) finalText.append(aPreview);
            finalText.append(rPreview);
            finalText.append(gPreview);
            finalText.append(bPreview);
            return finalText;
        }
        return baseText;
    }
}

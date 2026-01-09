package com.lx862.qomc.util;

import com.lx862.qomc.Platform;
import com.lx862.qomc.core.ValueType;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;
import java.util.Objects;

public class McUtil {
    public static Text configFeedback(TrackedValue<?> value, ValueType valueType) {
        MutableText keyName = Text.literal(QconfUtil.getDisplayOrDefaultName(value)).styled(s -> s
                .withFormatting(Formatting.GREEN)
                .withUnderline(true));

        MutableText text = Text.literal(" has been changed to ").append(McUtil.formatValue(value, valueType)).fillStyle(Style.EMPTY.withUnderline(false));
        return keyName.append(text);
    }

    public static MutableText fieldDetail(TrackedValue<?> trackedValue) {
        MutableText finalText = currentValue(trackedValue);

        if(trackedValue.hasMetadata(ChangeWarning.TYPE)) {
            finalText
                    .append("\n")
                    .append(changeWarning(trackedValue.metadata(ChangeWarning.TYPE)));
        }

        return finalText;
    }

    public static MutableText nodeBreadcrumb(Config config, ValueTreeNode node) {
        MutableText fieldHeader = Text.empty();

        for(int i = 0; i < node.key().length(); i++) {
            ValueTreeNode breadcrumbNode = config.getNode(QconfUtil.trimKey(node.key(), node.key().length()-1-i));
            MutableText nodeText;

            if(breadcrumbNode instanceof ValueTreeNode.Section) {
                nodeText = Text.literal("[" + breadcrumbNode.key().getLastComponent() + "]")
                        .fillStyle(Style.EMPTY.withColor(Formatting.GREEN));
            } else {
                nodeText = Text.literal(QconfUtil.getDisplayOrDefaultName(breadcrumbNode)).fillStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(true));
            }

            if(i != node.key().length()-1) { // Add hover text
                nodeText.styled(s -> s.withHoverEvent(Platform.hoverEventText(tooltip(breadcrumbNode))));
            }

            fieldHeader.append(nodeText);
            if(i != node.key().length()-1) {
                MutableText arrowText = Text.literal(" > ").fillStyle(Style.EMPTY.withColor(Formatting.GOLD));
                fieldHeader.append(arrowText);
            }
        }
        return fieldHeader;
    }

    public static MutableText nodeComments(ValueTreeNode node) {
        MutableText text = Text.empty().fillStyle(Style.EMPTY.withColor(Formatting.GRAY).withBold(false));
        if(node.hasMetadata(Comment.TYPE)) {
            boolean prependNewLine = false;
            for(String comment : node.metadata(Comment.TYPE)) {
                text.append(Text.literal((prependNewLine ? "\n" : "") + comment));
                prependNewLine = true;
            }
        }
        return text;
    }

    public static MutableText tooltip(ValueTreeNode node) {
        if(node instanceof ValueTreeNode.Section) {
            return Text.literal(QconfUtil.getDisplayOrDefaultName(node)).formatted(Formatting.GREEN).append("\n").append(nodeComments(node));
        } else {
            return Text.literal(QconfUtil.getDisplayOrDefaultName(node)).formatted(Formatting.WHITE).formatted(Formatting.BOLD).append("\n").append(nodeComments(node));
        }
    }

    public static MutableText valueOverview(TrackedValue<?> trackedValue) {
        MutableText text = Text.literal("- ").fillStyle(Style.EMPTY.withColor(Formatting.YELLOW));
        MutableText valueName = Text.literal(trackedValue.key().getLastComponent()).fillStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(false));
        valueName.styled(s -> s.withHoverEvent(Platform.hoverEventText(tooltip(trackedValue))));

        MutableText t3 = Text.literal(": ").fillStyle(Style.EMPTY.withColor(Formatting.WHITE).withBold(false));
        MutableText t4 = formatValue(trackedValue, ValueType.fromValue(trackedValue)).fillStyle(Style.EMPTY.withUnderline(true).withBold(false));

        return text.append(valueName).append(t3).append(t4);
    }

    public static MutableText valueType(TrackedValue<?> trackedValue) {
        ValueType valueType = ValueType.fromValue(trackedValue);
        MutableText finalText = Text.literal("Type: " + valueType.name);

        for(Constraint<?> constraint : trackedValue.constraints()) {
            if(constraint instanceof Constraint.Range<?> rangeConstraint) {
                Object low = rangeConstraint.min();
                Object high = rangeConstraint.max();

                MutableText rangeConstraintText = Text.literal(" [" + low + "-" + high + "]").formatted(Formatting.YELLOW);
                finalText.append(rangeConstraintText);
            } else if(constraint instanceof Constraint.All<?>) {
                MutableText constraintText = Text.literal(" [" + constraint.getRepresentation() + "]").formatted(Formatting.YELLOW);
                finalText.append(constraintText);
            }
        }

        return finalText;
    }

    private static MutableText currentValue(TrackedValue<?> trackedValue) {
        boolean valueIsDefault = Objects.equals(trackedValue.value(), trackedValue.getDefaultValue());
        MutableText headerText = Text.literal("Current Value: ");
        MutableText valueText = formatValue(trackedValue, ValueType.fromValue(trackedValue)).styled(s -> s.withUnderline(true));

        MutableText defaultText;

        if(valueIsDefault) {
            defaultText = Text.literal(" (Default)").formatted(Formatting.GRAY);
        } else {
            defaultText = Text.literal(" (Changed)")
                    .styled(s -> s.withHoverEvent(Platform.hoverEventText(Text.literal("Default: " + QconfUtil.stringify(trackedValue.getDefaultValue())).formatted(Formatting.GRAY))))
                    .formatted(Formatting.YELLOW);
        }
        return headerText.append(valueText).append(defaultText);
    }

    private static MutableText changeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning changeWarning) {
        MutableText headerText = Text.literal("Note: ").fillStyle(Style.EMPTY.withColor(Formatting.YELLOW).withBold(true));

        MutableText warningText =
            switch(changeWarning.getType()) {
                case Custom -> Text.literal(changeWarning.getCustomMessage());
                case CustomTranslatable -> Text.translatable(changeWarning.getCustomMessage());
                case RequiresRestart -> Text.literal("Restart is required for changes to apply");
                case Experimental -> Text.literal("Experimental option, use with caution!");
                case Unsafe -> Text.literal("Unsafe option, use at your own risk!");
            };

        warningText.fillStyle(Style.EMPTY.withColor(Formatting.YELLOW).withBold(false));
        return Text.literal("\n").append(headerText).append(warningText);
    }

    public static MutableText formatValue(TrackedValue<?> trackedValue, ValueType valueType) {
        if(valueType == ValueType.LIST) {
            MutableText text = Text.literal("[").styled(s -> s.withUnderline(false).withColor(Formatting.WHITE));
            ValueList<?> list = (ValueList<?>)trackedValue.value();
            ValueType listValueType = ValueType.fromValue(trackedValue, list.getDefaultValue());

            for(int i = 0; i < list.size(); i++) {
                Object innerValue = list.get(i);
                text.append(formatSimpleValue(innerValue, listValueType));
                if(i != list.size()-1) text.append(Text.literal(", ").styled(s -> s.withUnderline(false).withColor(Formatting.WHITE)));
            }
            text.append("]").styled(s -> s.withUnderline(false).withFormatting(Formatting.WHITE));
            return text;
        }

        return formatSimpleValue(trackedValue.value(), valueType);
    }

    private static MutableText formatSimpleValue(Object obj, ValueType valueType) {
        String str = QconfUtil.stringify(obj);
        MutableText baseText = Text.literal(str);
        if(valueType == ValueType.BOOLEAN) {
            if(str.equals("true")) return baseText.formatted(Formatting.GREEN);
            else return baseText.formatted(Formatting.RED);
        } else if(valueType == ValueType.INTEGER || valueType == ValueType.LONG || valueType == ValueType.DOUBLE || valueType == ValueType.FLOAT) {
            return baseText.formatted(Formatting.GOLD);
        } else if(valueType == ValueType.STRING) {
            return baseText.formatted(Formatting.AQUA);
        } else if(valueType == ValueType.ENUM) {
            return baseText.formatted(Formatting.BLUE);
        } else if(valueType == ValueType.COLOR_RGB || valueType == ValueType.COLOR_ARGB) {
            boolean hasAlpha = valueType == ValueType.COLOR_ARGB;
            String unquotedString = str.substring(1, str.length()-1);
            ColorUtil.ArgbColor color = ColorUtil.toArgbColor(unquotedString, true);
            int rgb = color.pack();

            MutableText finalText = Text.literal("#").fillStyle(Style.EMPTY.withColor(rgb));

            MutableText aPreview = Text.literal(ColorUtil.colorToHex(color.alpha()).toUpperCase(Locale.ROOT));
            MutableText rPreview = Text.literal(ColorUtil.colorToHex(color.red()).toUpperCase(Locale.ROOT));
            MutableText gPreview = Text.literal(ColorUtil.colorToHex(color.green()).toUpperCase(Locale.ROOT));
            MutableText bPreview = Text.literal(ColorUtil.colorToHex(color.blue()).toUpperCase(Locale.ROOT));

            if(hasAlpha) finalText.append(aPreview);
            finalText.append(rPreview);
            finalText.append(gPreview);
            finalText.append(bPreview);
            return finalText;
        }
        return baseText;
    }
}

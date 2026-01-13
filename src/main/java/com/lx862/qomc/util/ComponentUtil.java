package com.lx862.qomc.util;

import com.lx862.qomc.Platform;
import com.lx862.qomc.core.ValueType;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ComponentUtil {
    public static Component configFeedback(TrackedValue<?> value, ValueType valueType) {
        MutableComponent keyName = Component.literal(QconfUtil.getDisplayName(value)).withStyle(s -> s
                .applyFormat(ChatFormatting.GREEN)
                .withUnderlined(true));

        MutableComponent text = Component.literal(" has been set to ").withStyle(s -> s.withUnderlined(false)).append(ComponentUtil.formatValue(value, valueType));
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
                nodeText = Component.literal(QconfUtil.getDisplayName(breadcrumbNode)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(true));
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
            return Component.literal(QconfUtil.getDisplayName(node)).withStyle(ChatFormatting.GREEN).append("\n").append(configNodeComments(node));
        } else {
            return Component.literal(QconfUtil.getDisplayName(node)).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD).append("\n").append(configNodeComments(node));
        }
    }

    public static MutableComponent valueOverview(TrackedValue<?> trackedValue) {
        MutableComponent text = Component.literal("* ").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
        MutableComponent valueName = Component.literal(trackedValue.key().getLastComponent()).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false));

        MutableComponent t3 = Component.literal(": ").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false));
        MutableComponent t4 = formatValue(trackedValue, ValueType.getType(trackedValue)).withStyle(Style.EMPTY.withBold(false));
        return text.append(valueName).append(t3).append(t4);
    }

    public static MutableComponent valueType(TrackedValue<?> trackedValue) {
        ValueType valueType = ValueType.getType(trackedValue);
        return Component.literal("Type: " + valueType.name).append(constraintsText(trackedValue.constraints()));
    }

    public static <T> MutableComponent constraintsText(Iterable<Constraint<T>> constraints) {
        MutableComponent finalText = Component.empty();

        for(Constraint<?> constraint : constraints) {
            finalText.append("\n");
            String constraintStr;
            if(constraint instanceof Constraint.Range<?>) {
                Constraint.Range<?> rangeConstraint = (Constraint.Range<?>)constraint;
                boolean hasLowerConstraint = false;
                boolean hasUpperConstraint = false;

                Object lower = rangeConstraint.min();
                Object upper = rangeConstraint.max();

                if(lower instanceof Integer) {
                    hasLowerConstraint = (Integer)lower != Integer.MIN_VALUE;
                    hasUpperConstraint = (Integer)upper != Integer.MAX_VALUE;
                }
                if(lower instanceof Double) {
                    hasLowerConstraint = (Double) lower != Double.MIN_VALUE;
                    hasUpperConstraint = (Double) upper != Double.MAX_VALUE;
                }
                if(lower instanceof Float) {
                    hasLowerConstraint = (Float)lower != Float.MIN_VALUE;
                    hasUpperConstraint = (Float)upper != Float.MAX_VALUE;
                }
                if(lower instanceof Long) {
                    hasLowerConstraint = (Long)lower != Long.MIN_VALUE;
                    hasUpperConstraint = (Long)upper != Long.MAX_VALUE;
                }

                if(hasLowerConstraint && !hasUpperConstraint) {
                    constraintStr = "Larger than " + lower;
                } else if(hasUpperConstraint && !hasLowerConstraint) {
                    constraintStr = "Smaller than " + upper;
                } else {
                    constraintStr = lower + " to " + upper;
                }
            } else if(ValueType.ARGB_CONSTRAINTS.stream().anyMatch(s -> constraint.getRepresentation().contains(s))) {
                constraintStr = "Color Hex (ARGB)";
            } else if(ValueType.RGB_CONSTRAINTS.stream().anyMatch(s -> constraint.getRepresentation().contains(s))) {
                constraintStr = "Color Hex (RGB)";
            } else {
                constraintStr = constraint.getRepresentation();
            }
            finalText.append(Component.literal("[" + constraintStr + "]").withStyle(ChatFormatting.YELLOW));
        }

        return finalText;
    }

    public static MutableComponent currentValue(TrackedValue<?> trackedValue) {
        boolean valueIsDefault = Objects.equals(trackedValue.value(), trackedValue.getDefaultValue());
        MutableComponent headerText = Component.literal("Current Value: ");
        MutableComponent valueText = formatValue(trackedValue, ValueType.getType(trackedValue));

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

        MutableComponent warningText = null;
        if(changeWarning.getType() == ChangeWarning.Type.Custom) {
            return Component.literal(changeWarning.getCustomMessage());
        } else if(changeWarning.getType() == ChangeWarning.Type.CustomTranslatable) {
            return Component.translatable(changeWarning.getCustomMessage());
        } else if(changeWarning.getType() == ChangeWarning.Type.RequiresRestart) {
            return Component.literal("Restart is required for changes to apply");
        } else if(changeWarning.getType() == ChangeWarning.Type.Experimental) {
            return Component.literal("Experimental option, use with caution!");
        } else if(changeWarning.getType() == ChangeWarning.Type.Unsafe) {
            return Component.literal("Unsafe option, use at your own risk!");
        }

        warningText.withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(false));
        return Component.literal("\n").append(headerText).append(warningText);
    }

    public static MutableComponent formatValue(TrackedValue<?> trackedValue, ValueType valueType) {
        boolean valueIsDefault = Objects.equals(trackedValue.getDefaultValue(), trackedValue.value());
        if(valueType == ValueType.LIST) {
            MutableComponent text = Component.literal("[").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE));
            ValueList<?> list = (ValueList<?>)trackedValue.value();
            ValueType listValueType = ValueType.getChildType(trackedValue, list.getDefaultValue());

            for(int i = 0; i < list.size(); i++) {
                Object innerValue = list.get(i);
                text.append(formatSimpleValue(innerValue, listValueType));
                if(i != list.size()-1) text.append(Component.literal(", ").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE)));
            }
            text.append("]").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE));
            return text;
        } else if(valueType == ValueType.MAP) {
            MutableComponent text = Component.literal("[").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE));
            ValueMap<?> map = (ValueMap<?>)trackedValue.value();

            for(Map.Entry<String, ?> entry : map.entrySet()) {
                text.append("\n");
                text.append("   ");
                text.append(Component.literal(entry.getKey() + ": ").withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true)));
                text.append(formatSimpleValue(entry.getValue(), ValueType.getChildType(trackedValue, entry.getValue())));
            }

            text.append("\n]").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE));
            return text;
        }

        return formatSimpleValue(trackedValue.value(), valueType).withStyle(s -> s.withUnderlined(!valueIsDefault));
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

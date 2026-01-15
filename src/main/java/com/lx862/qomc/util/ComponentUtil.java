package com.lx862.qomc.util;

import com.lx862.qomc.core.ValueType;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;

import java.util.*;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import static com.lx862.qomc.util.VersionUtil.*;

public class ComponentUtil {
    public static Component configFeedback(TrackedValue<?> value, ValueType valueType) {
        MutableComponent keyName = literalText(QconfUtil.getDisplayName(value)).withStyle(s -> s
                .applyFormat(ChatFormatting.GREEN)
                .withUnderlined(true));

        MutableComponent text = literalText(" has been set to ").withStyle(s -> s.withUnderlined(false))
        .append(ComponentUtil.formatValue(value, value.getRealValue(), valueType));
        return keyName.append(text);
    }

    public static MutableComponent configNodeBreadcrumb(Config config, ValueTreeNode node) {
        MutableComponent fieldHeader = emptyText();

        for(int i = 0; i < node.key().length(); i++) {
            ValueTreeNode breadcrumbNode = config.getNode(QconfUtil.trimKey(node.key(), node.key().length()-1-i));
            MutableComponent nodeText;

            if(breadcrumbNode instanceof ValueTreeNode.Section) {
                nodeText = literalText("[" + breadcrumbNode.key().getLastComponent() + "]")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN));
            } else {
                nodeText = literalText(QconfUtil.getDisplayName(breadcrumbNode)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(true));
            }

            if(i != node.key().length()-1) { // Add hover text
                nodeText.withStyle(s -> s.withHoverEvent(VersionUtil.hoverEventText(configNodeTooltip(breadcrumbNode))));
            }

            fieldHeader.append(nodeText);
            if(i != node.key().length()-1) {
                MutableComponent arrowText = literalText(" > ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD));
                fieldHeader.append(arrowText);
            }
        }
        return fieldHeader;
    }

    public static List<MutableComponent> configNodeComments(ValueTreeNode node) {
        List<MutableComponent> components = new ArrayList<>();
        if(node.hasMetadata(Comment.TYPE)) {
            for(String comment : node.metadata(Comment.TYPE)) {
                components.add(literalText(comment).withStyle(ChatFormatting.GRAY));
            }
        }
        return components;
    }

    public static MutableComponent configNodeTooltip(ValueTreeNode node) {
        if(node instanceof ValueTreeNode.Section) {
            return literalText(QconfUtil.getDisplayName(node)).withStyle(ChatFormatting.GREEN)
                    .append(configNodeComments(node).stream().reduce(emptyText(), (acc, e) -> acc.append("\n").append(e)).withStyle(s -> s.withBold(false)));
        } else {
            return literalText(QconfUtil.getDisplayName(node)).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)
                    .append(configNodeComments(node).stream().reduce(emptyText(), (acc, e) -> acc.append("\n").append(e)).withStyle(s -> s.withBold(false)));
        }
    }

    public static <T> MutableComponent valueOverview(TrackedValue<T> trackedValue, ValueType valueType) {
        MutableComponent text = literalText("* ").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW));
        MutableComponent valueName = literalText(QconfUtil.getSerializedName(trackedValue)).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withBold(false));

        MutableComponent t3 = literalText(": ").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false));
        MutableComponent t4 = formatValue(trackedValue, trackedValue.value(), valueType).withStyle(Style.EMPTY.withBold(false).withUnderlined(!Objects.equals(trackedValue.value(), trackedValue.getDefaultValue())));
        return text.append(valueName).append(t3).append(t4);
    }

    public static MutableComponent valueType(ValueType valueType) {
        return literalText("Type: " + valueType.name);
    }

    public static <T> MutableComponent constraints(Iterable<Constraint<T>> constraints) {
        MutableComponent finalText = emptyText();

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
            finalText.append(literalText("[" + constraintStr + "]").withStyle(ChatFormatting.YELLOW));
        }

        return finalText;
    }

    public static <T> MutableComponent currentValue(TrackedValue<T> trackedValue, ValueType valueType) {
        boolean valueIsDefault = Objects.equals(trackedValue.value(), trackedValue.getDefaultValue());
        boolean valueOverriden = trackedValue.isBeingOverridden();
        MutableComponent headerText = literalText("Current Value: ");
        MutableComponent valueText = formatValue(trackedValue, trackedValue.value(), valueType);

        MutableComponent valueStatusText;

        if(valueOverriden) {
            valueStatusText = literalText("(Overriden)")
                    .withStyle(s ->
                            s.withHoverEvent(VersionUtil.hoverEventText(
                                literalText("Value is overridden by one or more mods.\nReal value: ").withStyle(ChatFormatting.YELLOW)
                                .append(formatValue(trackedValue, trackedValue.getRealValue(), valueType))
                            ))
                    )
                    .withStyle(ChatFormatting.AQUA);
        } else if(valueIsDefault) {
            valueStatusText = literalText("(Default)").withStyle(ChatFormatting.GRAY);
        } else {
            valueStatusText = literalText("(Changed)")
                    .withStyle(s -> s.withHoverEvent(
                            VersionUtil.hoverEventText(
                                    literalText("Default: ").withStyle(ChatFormatting.GRAY)
                                    .append(formatValue(trackedValue, trackedValue.getDefaultValue(), valueType)))
                            )
                            .withUnderlined(true)
                    )
                    .withStyle(ChatFormatting.YELLOW);
        }
        return headerText.append(valueText).append(" ").append(valueStatusText);
    }

    public static MutableComponent configNodeChangeWarning(folk.sisby.kaleido.lib.quiltconfig.api.metadata.ChangeWarning changeWarning) {
        MutableComponent headerText = literalText("Note: ").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true));

        MutableComponent warningText = null;
        if(changeWarning.getType() == ChangeWarning.Type.Custom) {
            warningText = literalText(changeWarning.getCustomMessage());
        } else if(changeWarning.getType() == ChangeWarning.Type.CustomTranslatable) {
            warningText = translatableText(changeWarning.getCustomMessage());
        } else if(changeWarning.getType() == ChangeWarning.Type.RequiresRestart) {
            warningText = literalText("Restart is required for changes to apply");
        } else if(changeWarning.getType() == ChangeWarning.Type.Experimental) {
            warningText = literalText("Experimental option, use with caution!");
        } else if(changeWarning.getType() == ChangeWarning.Type.Unsafe) {
            warningText = literalText("Unsafe option, use at your own risk!");
        }

        warningText.withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(false));
        return headerText.append(warningText);
    }

    public static MutableComponent formatValue(TrackedValue<?> trackedValue, Object value, ValueType valueType) {
        if(valueType == ValueType.LIST) {
            MutableComponent text = literalText("[").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE));
            ValueList<?> list = (ValueList<?>)value;
            ValueType childType = ValueType.getType(trackedValue, list.getDefaultValue());

            for(int i = 0; i < list.size(); i++) {
                Object innerValue = list.get(i);
                text.append(formatSimpleValue(innerValue, childType));
                if(i != list.size()-1) text.append(literalText(", ").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE)));
            }
            text.append("]").withStyle(s -> s.withUnderlined(false).withColor(ChatFormatting.WHITE));
            return text;
        } else if(valueType == ValueType.MAP) {
            MutableComponent text = literalText("[").withStyle(ChatFormatting.WHITE);
            ValueMap<?> map = (ValueMap<?>)value;
            ValueType childType = ValueType.getType(trackedValue, map.getDefaultValue());

            for(Map.Entry<String, ?> entry : map.entrySet()) {
                text.append("\n");
                text.append(literalText("   ").withStyle(s -> s.withUnderlined(false))); // Indentation
                text.append(literalText(entry.getKey() + ": ").withStyle(s -> s.withColor(ChatFormatting.AQUA).withUnderlined(false)));
                text.append(formatSimpleValue(entry.getValue(), childType).withStyle(s -> s.withUnderlined(false)));
            }

            text.append("\n]").withStyle(ChatFormatting.WHITE);
            return text;
        }

        return formatSimpleValue(value, valueType);
    }

    private static MutableComponent formatSimpleValue(Object obj, ValueType valueType) {
        String str = QconfUtil.stringify(obj);
        MutableComponent baseText = literalText(str);
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

            MutableComponent finalText = literalText("#").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)));

            MutableComponent aPreview = literalText(ColorUtil.colorToHex(color.alpha()).toUpperCase(Locale.ROOT));
            MutableComponent rPreview = literalText(ColorUtil.colorToHex(color.red()).toUpperCase(Locale.ROOT));
            MutableComponent gPreview = literalText(ColorUtil.colorToHex(color.green()).toUpperCase(Locale.ROOT));
            MutableComponent bPreview = literalText(ColorUtil.colorToHex(color.blue()).toUpperCase(Locale.ROOT));

            if(hasAlpha) finalText.append(aPreview);
            finalText.append(rPreview);
            finalText.append(gPreview);
            finalText.append(bPreview);
            return finalText;
        }
        return baseText;
    }
}

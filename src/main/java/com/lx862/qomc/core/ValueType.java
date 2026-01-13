package com.lx862.qomc.core;

import folk.sisby.kaleido.lib.quiltconfig.api.Constraint;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueMap;

import java.util.Arrays;
import java.util.List;

public enum ValueType {
    BOOLEAN("Boolean"),
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    FLOAT("Float"),
    DOUBLE("Double"),
    ENUM("Enum"),
    COLOR_RGB("Color (RGB)"),
    COLOR_ARGB("Color (ARGB)"),
    LIST("List"),
    MAP("Map (Key-Value)"),
    UNKNOWN("Unknown");

    public static final List<String> RGB_CONSTRAINTS = Arrays.asList(
            "matches r'#[0-9a-fA-F]{6}'",
            "matches r'#[0-9A-Fa-f]{6}'",
            "matches r'#[a-fA-F0-9]{6}'",
            "matches r'#[A-Fa-f0-9]{6}'"
    );

    public static final List<String> ARGB_CONSTRAINTS = Arrays.asList(
            "matches r'#[0-9a-fA-F]{8}'",
            "matches r'#[0-9A-Fa-f]{8}'",
            "matches r'#[a-fA-F0-9]{8}'",
            "matches r'#[A-Fa-f0-9]{8}'"
    );

    public final String name;

    ValueType(String name) {
        this.name = name;
    }

    private static ValueType probeColorConstraint(TrackedValue<?> parentField) {
        boolean color = false;
        boolean alpha = false;
        for (Constraint<?> constraint : parentField.constraints()) {
            if (RGB_CONSTRAINTS.stream().anyMatch(s -> constraint.getRepresentation().contains(s))) color = true;
            if (ARGB_CONSTRAINTS.stream().anyMatch(s -> constraint.getRepresentation().contains(s))) {
                color = true;
                alpha = true;
            }
        }
        if(color && alpha) return COLOR_ARGB;
        if(color) return COLOR_RGB;
        return null;
    }

    public static ValueType getChildType(TrackedValue<?> parentValue, Object childValue) {
        ValueType colorType = probeColorConstraint(parentValue);
        return colorType == null ? getType(childValue) : colorType;
    }

    public static ValueType getType(TrackedValue<?> field) {
        Object obj = field.getDefaultValue();
        ValueType colorType = probeColorConstraint(field);

        if(obj instanceof String && colorType != null) return colorType;
        return getType(obj);
    }

    private static ValueType getType(Object obj) {
        if(obj instanceof Boolean) return BOOLEAN;
        if(obj instanceof String) return STRING;
        if(obj instanceof Integer) return INTEGER;
        if(obj instanceof Long) return LONG;
        if(obj instanceof Float) return FLOAT;
        if(obj instanceof Double) return DOUBLE;
        if(obj instanceof Enum<?>) return ENUM;
        if(obj instanceof ValueList<?>) return LIST;
        if(obj instanceof ValueMap<?>) return MAP;
        return UNKNOWN;
    }
}

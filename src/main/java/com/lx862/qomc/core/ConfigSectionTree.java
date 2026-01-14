package com.lx862.qomc.core;

import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class ConfigSectionTree {
    private final ValueTreeNode node;
    private final List<TrackedValue<?>> fields;
    private final HashMap<ValueKey, ConfigSectionTree> sections;

    public ConfigSectionTree(ValueTreeNode node, List<TrackedValue<?>> fields, HashMap<ValueKey, ConfigSectionTree> sections) {
        this.node = node;
        this.fields = fields;
        this.sections = sections;
    }

    public static ConfigSectionTree create(ValueTreeNode sectionNode) {
        return new ConfigSectionTree(sectionNode, new ArrayList<>(), new HashMap<>(0));
    }

    public List<TrackedValue<?>> fields() {
        return this.fields;
    }

    public HashMap<ValueKey, ConfigSectionTree> sections() {
        return this.sections;
    }

    public ValueTreeNode node() {
        return this.node;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        ConfigSectionTree other = (ConfigSectionTree)o;
        return Objects.equals(other.node, this.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.node);
    }
}

package com.lx862.qomc.core;

import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class ConfigSectionTree {
    private final List<TrackedValue<?>> fields;
    private final HashMap<ValueKey, ConfigSectionTree> sections;

    public ConfigSectionTree(List<TrackedValue<?>> fields, HashMap<ValueKey, ConfigSectionTree> sections) {
        this.fields = fields;
        this.sections = sections;
    }

    public static ConfigSectionTree create() {
        return new ConfigSectionTree(new ArrayList<>(), new HashMap<>(0));
    }

    public List<TrackedValue<?>> fields() {
        return this.fields;
    }

    public HashMap<ValueKey, ConfigSectionTree> sections() {
        return this.sections;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        ConfigSectionTree other = (ConfigSectionTree)o;
        return Objects.equals(other.fields, this.fields) && Objects.equals(other.sections, this.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fields, this.sections);
    }
}

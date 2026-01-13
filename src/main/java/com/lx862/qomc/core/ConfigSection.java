package com.lx862.qomc.core;

import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class ConfigSection {
    private final List<TrackedValue<?>> fields;
    private final HashMap<ValueKey, ConfigSection> sections;

    public ConfigSection(List<TrackedValue<?>> fields, HashMap<ValueKey, ConfigSection> sections) {
        this.fields = fields;
        this.sections = sections;
    }

    public static ConfigSection create() {
        return new ConfigSection(new ArrayList<>(), new HashMap<>(0));
    }

    public List<TrackedValue<?>> fields() {
        return this.fields;
    }

    public HashMap<ValueKey, ConfigSection> sections() {
        return this.sections;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        ConfigSection other = (ConfigSection)o;
        return Objects.equals(other.fields, this.fields) && Objects.equals(other.sections, this.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.fields, this.sections);
    }
}

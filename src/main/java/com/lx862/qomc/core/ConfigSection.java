package com.lx862.qomc.core;

import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public record ConfigSection(List<TrackedValue<?>> fields, HashMap<ValueKey, ConfigSection> sections) {
    public static ConfigSection create() {
        return new ConfigSection(new ArrayList<>(), new HashMap<>(0));
    }
}

package com.lx862.qomc.util;

import java.util.Objects;

public final class ModInfo {
    private final String name;
    private final String version;
    private final String id;

    public ModInfo(String name, String version, String id) {
        this.name = name;
        this.version = version;
        this.id = id;
    }

    public String name() {
        return this.name;
    }

    public String version() {
        return this.version;
    }

    public String id() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || o.getClass() != this.getClass()) return false;
        ModInfo other = (ModInfo)o;
        return Objects.equals(other.id(), this.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version, id);
    }
}

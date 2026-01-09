package com.lx862.qomc.util;

import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueKeyImpl;

import java.awt.*;

public class QconfUtil {
    public static String getFamilyOrId(Config config) {
        return config.family().isEmpty() ? config.id() : config.family();
    }

    public static ValueKey trimKey(ValueKey valueKey, int trimAmount) {
        String[] keys = new String[valueKey.length()-trimAmount];

        for(int i = 0; i < valueKey.length()-trimAmount; i++) {
            keys[i] = valueKey.getKeyComponent(i);
        }

        return new ValueKeyImpl(keys);
    }

    public static NameSet getFieldName(ValueTreeNode node) {
        String key = node.key().getLastComponent();
        String displayName;
        if(node.hasMetadata(DisplayName.TYPE)) {
            displayName = node.metadata(DisplayName.TYPE).getName();
        } else {
            displayName = null;
        }
        return new NameSet(key, displayName);
    }

    public static String getDisplayOrDefaultName(ValueTreeNode node) {
        NameSet nameSet = getFieldName(node);
        if(nameSet.displayName() != null) return nameSet.displayName();
        else return nameSet.key();
    }

    public static String stringify(Object o) {
        if(o instanceof ValueList<?> list) {
            StringBuilder sb = new StringBuilder("[");

            for(int i = 0; i < list.size(); i++) {
                sb.append(stringify(list.get(i)));
                if(i != list.size()-1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
        if(o instanceof String) {
            return "\"" + o + "\"";
        }
        return o.toString();
    }

    public record NameSet(String key, String displayName) {}
}

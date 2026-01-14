package com.lx862.qomc.util;

import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayName;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.DisplayNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.SerializedNameConvention;
import folk.sisby.kaleido.lib.quiltconfig.api.metadata.MetadataContainer;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueKey;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueList;
import folk.sisby.kaleido.lib.quiltconfig.api.values.ValueTreeNode;
import folk.sisby.kaleido.lib.quiltconfig.impl.values.ValueKeyImpl;

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

    public static String getSerializedName(ValueTreeNode node) {
        if(node.hasMetadata(SerializedNameConvention.TYPE)) {
            return node.metadata(SerializedNameConvention.TYPE).coerce(node.key().getLastComponent());
        } else {
            return node.key().getLastComponent();
        }
    }

    public static String getDisplayName(ValueTreeNode node) {
        return getDisplayName(node, getSerializedName(node));
    }

    public static String getDisplayName(MetadataContainer node, String key) {
        String displayName;
        if(node.hasMetadata(DisplayName.TYPE)) {
            displayName = node.metadata(DisplayName.TYPE).getName();
        } else if(node.hasMetadata(DisplayNameConvention.TYPE)) {
            displayName = node.metadata(DisplayNameConvention.TYPE).coerce(key);
        } else {
            displayName = null;
        }
        return displayName == null ? key : displayName;
    }

    public static String stringify(Object o) {
        if(o instanceof ValueList<?>) {
            ValueList<?> list = (ValueList<?>)o;
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
}

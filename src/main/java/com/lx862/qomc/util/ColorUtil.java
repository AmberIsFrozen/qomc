package com.lx862.qomc.util;

import org.apache.commons.lang3.StringUtils;

public class ColorUtil {
    public static ArgbColor toArgbColor(String string, boolean alpha) throws NumberFormatException {
        String hex = string.replace("#", "");
        int i = alpha ? Integer.parseUnsignedInt(hex, 16) : Integer.parseInt(hex, 16);
        int a = alpha ? (i >> 24) & 0xFF : 0;
        int r = (i >> 16) & 0x00FF;
        int g = (i >> 8) & 0x0000FF;
        int b = i & 0x000000FF;

        return new ArgbColor(a, r, g, b);
    }

    public static String colorToHex(ArgbColor color, boolean alpha) {
        String s = "#";
        if(alpha) {
            s += colorToHex(color.alpha());
        }
        s += colorToHex(color.red());
        s += colorToHex(color.green());
        s += colorToHex(color.blue());
        return s;
    }

    public static String colorToHex(int colorChannel) {
        return StringUtils.leftPad(Integer.toHexString(colorChannel), 2, "0");
    }

    public record ArgbColor(int alpha, int red, int green, int blue) {
        public int pack() {
            return this.alpha << 24 | red << 16 | green << 8 | blue;
        }
    };
}

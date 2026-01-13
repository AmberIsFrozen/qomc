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

    public static final class ArgbColor {
        private final int alpha;
        private final int red;
        private final int green;
        private final int blue;

        public ArgbColor(int alpha, int red, int green, int blue) {
            this.alpha = alpha;
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        public int alpha() {
            return this.alpha;
        }

        public int red() {
            return this.red;
        }

        public int green() {
            return this.green;
        }

        public int blue() {
            return this.blue;
        }

        public int pack() {
            return this.alpha << 24 | red << 16 | green << 8 | blue;
        }
    }
}

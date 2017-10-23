package org.janelia.it.jacs.shared.utils;

import java.awt.Color;

/**
 * Utilities for dealing with representation of colors in the domain model.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorUtils {

    /**
     * Convert a Java AWT color (with alpha channel) to a hex string like "#RRGGBBAA"
     * @param color
     * @return
     */
    public static String toHex(Color color) {
        return String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Convert a hex representation of a color (with or without alpha channel) that looks like
     * "#RRGGGBB" or "RRGGBB" or "#RRGGBBAA" or "RRGGBBAA" into a Java AWT color.
     * @param hexColor
     * @return
     */
    public static Color fromHex(String hexColor) {
        if (!hexColor.startsWith("#")) hexColor = "#"+hexColor;
        int r = Integer.valueOf(hexColor.substring( 1, 3 ), 16);
        int g = Integer.valueOf(hexColor.substring( 3, 5 ), 16);
        int b = Integer.valueOf(hexColor.substring( 5, 7 ), 16);
        if (hexColor.length()>6) {
            int a = Integer.valueOf(hexColor.substring(7, 9), 16);
            return new Color(r, g, b, a);
        }
        else {
            return new Color(r, g, b);
        }
    }

    public static void main(String[] args) {
        // TODO: make these into unit tests
        System.out.println(""+toHex(new Color(45, 34, 23)));
        System.out.println(""+toHex(new Color(12, 166, 2, 244)));
        Color c1 = new Color(45, 34, 23);
        Color c2 = new Color(45, 34, 23, 56);
        System.out.println(fromHex(toHex(c1)).equals(c1));
        System.out.println(fromHex(toHex(c2)).equals(c2));
    }

}

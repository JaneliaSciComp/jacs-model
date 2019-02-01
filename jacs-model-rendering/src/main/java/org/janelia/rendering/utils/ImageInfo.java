package org.janelia.rendering.utils;

import java.awt.image.ColorModel;

public class ImageInfo {
    public final int sx;
    public final int sy;
    public final int sz;
    public final int bitsPerPixel;
    public final ColorModel colorModel;

    public ImageInfo(int sx, int sy, int sz, int bitsPerPixel, ColorModel colorModel) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.bitsPerPixel = bitsPerPixel;
        this.colorModel = colorModel;
    }
}

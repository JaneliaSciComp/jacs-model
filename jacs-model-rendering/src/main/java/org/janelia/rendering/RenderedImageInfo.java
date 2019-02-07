package org.janelia.rendering;

public class RenderedImageInfo {
    public final int sx;
    public final int sy;
    public final int sz;
    public final int cmPixelSize;
    public final boolean sRGBspace;

    public RenderedImageInfo(int sx, int sy, int sz, int cmPixelSize, boolean sRGBspace) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.cmPixelSize = cmPixelSize;
        this.sRGBspace = sRGBspace;
    }
}

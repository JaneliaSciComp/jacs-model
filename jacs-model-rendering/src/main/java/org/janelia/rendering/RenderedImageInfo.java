package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class RenderedImageInfo {
    public final int sx;
    public final int sy;
    public final int sz;
    public final int cmPixelSize;
    public final boolean sRGBspace;

    @JsonCreator
    public RenderedImageInfo(@JsonProperty("sx") int sx,
                             @JsonProperty("sy") int sy,
                             @JsonProperty("sz") int sz,
                             @JsonProperty("cmPixelSize") int cmPixelSize,
                             @JsonProperty("sRGBspace") boolean sRGBspace) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.cmPixelSize = cmPixelSize;
        this.sRGBspace = sRGBspace;
    }

    /**
     * @return the number of bytes per pixel. If the bits per pixel is < 8 the return value may be < 1.
     */
    @JsonIgnore
    public double getBytesPerPixel() {
        return cmPixelSize / 8.;
    }
}

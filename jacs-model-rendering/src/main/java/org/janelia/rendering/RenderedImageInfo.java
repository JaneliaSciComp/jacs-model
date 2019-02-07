package org.janelia.rendering;

import com.fasterxml.jackson.annotation.JsonCreator;
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
}

package org.janelia.rendering.ymlrepr;

import java.util.List;

/**
 * YML representation for the image acquisition.
 */
public class RawVolData {
    private String path;
    private List<RawTile> tiles;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<RawTile> getTiles() {
        return tiles;
    }

    public void setTiles(List<RawTile> tiles) {
        this.tiles = tiles;
    }
}

package org.janelia.model.domain.gui.colordepth;

import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.interfaces.HasName;

import java.util.ArrayList;
import java.util.List;

/**
 * A color depth mask is an image file which is used to search against the
 * color depth image database. It has an internal id which is only used in the context
 * of a ColorDepthSearch.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthMask implements HasFilepath, HasName {

    private String name;
    private String filepath;
    private List<ColorDepthResult> results = new ArrayList<>();

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFilepath() {
        return filepath;
    }

    @Override
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public List<ColorDepthResult> getResults() {
        return results;
    }

    public void setResults(List<ColorDepthResult> results) {
        if (results==null) throw new IllegalArgumentException("Property cannot be null");
        this.results = results;
    }

    public void addResult(ColorDepthResult result) {
        results.add(result);
    }
}

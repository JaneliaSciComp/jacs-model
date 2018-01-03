package org.janelia.model.domain.gui.colordepth;

import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.workspace.TreeNode;

/**
 * A color depth mask is an image file which is used to search against the
 * color depth image database. Once the search is completed, the results are added
 * as children of this node.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthMask extends TreeNode implements HasFilepath {

    private String filepath;

    @Override
    public String getFilepath() {
        return filepath;
    }

    @Override
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }


}

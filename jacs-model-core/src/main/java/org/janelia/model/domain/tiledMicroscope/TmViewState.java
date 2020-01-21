package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.support.SearchAttribute;

public class TmViewState {
    Boolean visible;
    Boolean selected;
    String colorHex;

    @SearchAttribute(key = "visible_b", label = "Visibility")
    @JsonIgnore
    public Boolean getVisibility() {
        return visible;
    }

    public boolean isVisible() {
        return visible == null || visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

}

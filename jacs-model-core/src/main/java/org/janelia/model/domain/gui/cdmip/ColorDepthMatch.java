package org.janelia.model.domain.gui.cdmip;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.janelia.model.domain.Reference;

/**
 * A match made with a ColorDepthSearch.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthMatch {

    private Reference imageRef;
    private Integer score;
    private Double scorePercent;

    public Reference getImageRef() {
        return imageRef;
    }

    public void setImageRef(Reference imageRef) {
        this.imageRef = imageRef;
    }
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer scoreAbs) {
        this.score = scoreAbs;
    }

    public Double getScorePercent() {
        return scorePercent;
    }

    public void setScorePercent(Double scorePercent) {
        this.scorePercent = scorePercent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("imageRef", imageRef)
                .append("score", score)
                .append("scorePercent", scorePercent)
                .toString();
    }
}

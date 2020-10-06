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
    private Reference matchingImageRef;
    private Integer score;
    private Double scorePercent;
    private Integer matchingPixels;
    private Double matchingPixelsRatio;
    private Long gradientAreaGap;
    private Long highExpressionArea;

    public Reference getImageRef() {
        return imageRef;
    }

    public void setImageRef(Reference imageRef) {
        this.imageRef = imageRef;
    }

    public Reference getMatchingImageRef() {
        return matchingImageRef;
    }

    public void setMatchingImageRef(Reference matchingImageRef) {
        this.matchingImageRef = matchingImageRef;
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

    public Integer getMatchingPixels() {
        return matchingPixels;
    }

    public void setMatchingPixels(Integer matchingPixels) {
        this.matchingPixels = matchingPixels;
    }

    public Double getMatchingPixelsRatio() {
        return matchingPixelsRatio;
    }

    public void setMatchingPixelsRatio(Double matchingPixelsRatio) {
        this.matchingPixelsRatio = matchingPixelsRatio;
    }

    public Long getGradientAreaGap() {
        return gradientAreaGap;
    }

    public void setGradientAreaGap(Long gradientAreaGap) {
        this.gradientAreaGap = gradientAreaGap;
    }

    public Long getHighExpressionArea() {
        return highExpressionArea;
    }

    public void setHighExpressionArea(Long highExpressionArea) {
        this.highExpressionArea = highExpressionArea;
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

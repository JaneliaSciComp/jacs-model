package org.janelia.model.domain.sample;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.interfaces.HasFiles;
import org.janelia.model.domain.support.MongoMapped;

import java.util.HashMap;
import java.util.Map;

/**
 * A PublishedImage is an image that has been uploaded typically to an AWS S3 bucket for
 * use in a website. Given a slide code, objective, and alignment space, it provides
 * URLs for such corresponding uploaded images.
 *
 * @author <a href="mailto:olbrisd@janelia.hhmi.org">Donald J. Olbris</a>
 */
@MongoMapped(collectionName="publishedImage", label="Published Image")
public class PublishedImage extends AbstractDomainObject implements HasFiles {

    private Reference sampleRef;

    private String line;
    private String area;
    private String tile;
    private String releaseName;
    private String slideCode;
    private String objective;
    private String alignmentSpace;

    private Map<FileType, String> files = new HashMap<>();

    public Reference getSampleRef() {
        return sampleRef;
    }

    public void setSampleRef(Reference sampleRef) {
        this.sampleRef = sampleRef;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTile() {
        return tile;
    }

    public void setTile(String tile) {
        this.tile = tile;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getSlideCode() {
        return slideCode;
    }

    public void setSlideCode(String slideCode) {
        this.slideCode = slideCode;
    }

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    @Override
    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }
}

package org.janelia.it.jacs.model.sage;

// Generated Jan 14, 2014 11:00:59 AM by Hibernate Tools 3.4.0.CR1

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Image generated by hbm2java
 */
public class Image implements Serializable {

    private Integer id;
    private CvTerm family;
    private Line line;
    private CvTerm source;
    private String name;
    private String url;
    private String path;
    private String jfsPath;
    private Experiment experiment;
    private Date captureDate;
    private boolean representative;
    private boolean display;
    private String createdBy;
    private Date createDate;
    private Set<ImageProperty> imageProperties = new HashSet<ImageProperty>(0);
    private Set<SecondaryImage> secondaryImages = new HashSet<SecondaryImage>(0);

    public Image() {
    }

    public Image(CvTerm family, Line line, CvTerm source, String name, String url, String path, boolean representative,
            boolean display, String createdBy, Date createDate) {
        this.family = family;
        this.line = line;
        this.source = source;
        this.name = name;
        this.url = url;
        this.path = path;
        this.representative = representative;
        this.display = display;
        this.createdBy = createdBy;
        this.createDate = createDate;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CvTerm getFamily() {
        return this.family;
    }

    public void setFamily(CvTerm family) {
        this.family = family;
    }

    public Line getLine() {
        return this.line;
    }

    public void setLine(Line line) {
        this.line = line;
    }

    public CvTerm getSource() {
        return this.source;
    }

    public void setSource(CvTerm source) {
        this.source = source;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getJfsPath() {
        return this.jfsPath;
    }

    public void setJfsPath(String jfsPath) {
        this.jfsPath = jfsPath;
    }
    
    public Experiment getExperiment() {
        return this.experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public Date getCaptureDate() {
        return this.captureDate;
    }

    public void setCaptureDate(Date captureDate) {
        this.captureDate = captureDate;
    }

    public boolean getRepresentative() {
        return this.representative;
    }

    public void setRepresentative(boolean representative) {
        this.representative = representative;
    }

    public boolean isDisplay() {
        return this.display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Set<ImageProperty> getImageProperties() {
        return this.imageProperties;
    }

    public void setImageProperties(Set<ImageProperty> imageProperties) {
        this.imageProperties = imageProperties;
    }

    public Set<SecondaryImage> getSecondaryImages() {
        return this.secondaryImages;
    }

    public void setSecondaryImages(Set<SecondaryImage> secondaryImages) {
        this.secondaryImages = secondaryImages;
    }
}

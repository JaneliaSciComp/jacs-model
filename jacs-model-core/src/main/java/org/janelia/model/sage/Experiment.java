package org.janelia.model.sage;

// Generated Nov 2, 2015 2:36:55 PM by Hibernate Tools 3.4.0.CR1

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Experiment generated by hbm2java
 */
public class Experiment implements java.io.Serializable {

    private Integer id;
    private CvTerm type;
    private CvTerm lab;
    private String name;
    private String experimenter;
    private Date createDate;
    private Set<Image> images = new HashSet<>(0);
    private Set<ExperimentProperty> properties = new HashSet<>(0);
    private Set<Observation> observations = new HashSet<>(0);
    private Set<SageSession> sessions = new HashSet<>(0);

    public Experiment() {
    }

    public Experiment(CvTerm type, CvTerm lab, String name, String experimenter, Date createDate) {
        this.type = type;
        this.lab = lab;
        this.name = name;
        this.experimenter = experimenter;
        this.createDate = createDate;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CvTerm getType() {
        return this.type;
    }

    public void setType(CvTerm type) {
        this.type = type;
    }

    public CvTerm getLab() {
        return this.lab;
    }

    public void setLab(CvTerm lab) {
        this.lab = lab;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExperimenter() {
        return this.experimenter;
    }

    public void setExperimenter(String experimenter) {
        this.experimenter = experimenter;
    }

    public Date getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Set<Image> getImages() {
        return this.images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    public Set<ExperimentProperty> getExperimentProperties() {
        return this.properties;
    }

    public void setExperimentProperties(Set<ExperimentProperty> properties) {
        this.properties = properties;
    }

    public Set<Observation> getObservations() {
        return this.observations;
    }

    public void setObservations(Set<Observation> observations) {
        this.observations = observations;
    }

    public Set<SageSession> getSessions() {
        return this.sessions;
    }

    public void setSessions(Set<SageSession> sessions) {
        this.sessions = sessions;
    }

}

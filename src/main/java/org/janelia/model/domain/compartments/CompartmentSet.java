package org.janelia.model.domain.compartments;

import java.util.ArrayList;
import java.util.List;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.interfaces.HasFilepath;
import org.janelia.model.domain.support.MongoMapped;

import com.fasterxml.jackson.annotation.JsonProperty;

@MongoMapped(collectionName="compartmentSet",label="Compartment Set")
public class CompartmentSet extends AbstractDomainObject implements HasFilepath {

    private String filepath;
    private String imageSize;
    private String opticalResolution;
    private String alignmentSpace;
    private List<Compartment> compartments = new ArrayList<>();

    @Override
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getImageSize() {
        return imageSize;
    }

    public void setImageSize(String imageSize) {
        this.imageSize = imageSize;
    }

    public String getOpticalResolution() {
        return opticalResolution;
    }

    public void setOpticalResolution(String opticalResolution) {
        this.opticalResolution = opticalResolution;
    }

    public String getAlignmentSpace() {
        return alignmentSpace;
    }

    public void setAlignmentSpace(String alignmentSpace) {
        this.alignmentSpace = alignmentSpace;
    }

    @JsonProperty
    public List<Compartment> getCompartments() {
        return compartments;
    }

    @JsonProperty
    public void setCompartments(List<Compartment> compartments) {
        if (compartments==null) throw new IllegalArgumentException("Property cannot be null");
        this.compartments = compartments;
        for (Compartment compartment : compartments) {
            compartment.setParent(this);
        }
    }

    public Compartment getCompartment(Long id) {
        if (id==null) return null;
        for(Compartment compartment : compartments) {
            if (compartment.getId().equals(id)) {
                return compartment;
            }
        }
        return null;
    }

    public Compartment getCompartmentByName(String name) {
        if (name==null) return null;
        for(Compartment compartment : compartments) {
            if (compartment.getName().equals(name)) {
                return compartment;
            }
        }
        return null;
    }

}

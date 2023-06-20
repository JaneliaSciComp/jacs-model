package org.janelia.model.domain.sample;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;
import org.janelia.model.domain.workspace.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * A release definition which controls a set of Samples which are to be publicly released
 * and published to an external website.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="release",label="Line Release")
@SearchType(key="release",label="Line Release")
public class LineRelease extends AbstractDomainObject implements Node {

    /** Valid values for targetWebsite attribute */
    @JsonIgnore
    public static final String[] TARGET_WEBSITES = {"Split GAL4", "Gen1 MCFO", "FLEW", "Raw"};

    private boolean sageSync;

    private String targetWebsite;

    private List<String> annotators = new ArrayList<>();

    @SearchTraversal({})
    private List<Reference> children = new ArrayList<>();

    public String getTargetWebsite() {
        return targetWebsite;
    }

    public void setTargetWebsite(String targetWebsite) {
        this.targetWebsite = targetWebsite;
    }

    @Override
    public List<Reference> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Reference> children) {
        if (children==null) throw new IllegalArgumentException("Property cannot be null");
        this.children = children;
    }

    public boolean isSageSync() {
        return sageSync;
    }

    public void setSageSync(boolean sageSync) {
        this.sageSync = sageSync;
    }

    public List<String> getAnnotators() {
        return annotators;
    }

    public void setAnnotators(List<String> annotators) {
        if (annotators==null) throw new IllegalArgumentException("Property cannot be null");
        this.annotators = annotators;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LineRelease [");
        builder.append("sageSync=");
        builder.append(sageSync);
        builder.append(", ");
        if (annotators != null) {
            builder.append("annotators=");
            builder.append(annotators);
            builder.append(", ");
        }
        if (getOwnerName() != null) {
            builder.append("getOwnerName()=");
            builder.append(getOwnerName());
            builder.append(", ");
        }
        if (getId() != null) {
            builder.append("getId()=");
            builder.append(getId());
            builder.append(", ");
        }
        if (getName() != null) {
            builder.append("getName()=");
            builder.append(getName());
        }
        builder.append("]");
        return builder.toString();
    }
    
}

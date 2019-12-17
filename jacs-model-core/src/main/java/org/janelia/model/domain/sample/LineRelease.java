package org.janelia.model.domain.sample;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.workspace.Node;

/**
 * A release definition which controls how Samples are released and published to the external website. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="release",label="Line Release")
public class LineRelease extends AbstractDomainObject implements Node {

    @Deprecated
	private boolean autoRelease;

    @Deprecated
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date releaseDate;

    @Deprecated
    private Integer lagTimeMonths;

    @Deprecated
    private boolean sageSync;

    @Deprecated
    private List<String> dataSets = new ArrayList<>();

    private List<String> annotators = new ArrayList<>();

    @Deprecated
    private List<String> subscribers = new ArrayList<>();

    @SearchTraversal({})
    private List<Reference> children = new ArrayList<>();

    @Override
    public List<Reference> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<Reference> children) {
        if (children==null) throw new IllegalArgumentException("Property cannot be null");
        this.children = children;
    }

	public boolean isAutoRelease() {
		return autoRelease;
	}

	public void setAutoRelease(boolean autoRelease) {
		this.autoRelease = autoRelease;
	}

	public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getLagTimeMonths() {
        return lagTimeMonths;
    }

    public void setLagTimeMonths(Integer lagTimeMonths) {
        this.lagTimeMonths = lagTimeMonths;
    }

    public boolean isSageSync() {
        return sageSync;
    }

    public void setSageSync(boolean sageSync) {
        this.sageSync = sageSync;
    }

    public List<String> getDataSets() {
        return dataSets;
    }

    public void setDataSets(List<String> dataSets) {
        if (dataSets==null) throw new IllegalArgumentException("Property cannot be null");
        this.dataSets = dataSets;
    }

    public List<String> getAnnotators() {
        return annotators;
    }

    public void setAnnotators(List<String> annotators) {
        if (annotators==null) throw new IllegalArgumentException("Property cannot be null");
        this.annotators = annotators;
    }

    public List<String> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(List<String> subscribers) {
        if (subscribers==null) throw new IllegalArgumentException("Property cannot be null");
        this.subscribers = subscribers;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LineRelease [");
        if (releaseDate != null) {
            builder.append("releaseDate=");
            builder.append(releaseDate);
            builder.append(", ");
        }
        if (lagTimeMonths != null) {
            builder.append("lagTimeMonths=");
            builder.append(lagTimeMonths);
            builder.append(", ");
        }
        builder.append("sageSync=");
        builder.append(sageSync);
        builder.append(", ");
        if (dataSets != null) {
            builder.append("dataSets=");
            builder.append(dataSets);
            builder.append(", ");
        }
        if (annotators != null) {
            builder.append("annotators=");
            builder.append(annotators);
            builder.append(", ");
        }
        if (subscribers != null) {
            builder.append("subscribers=");
            builder.append(subscribers);
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

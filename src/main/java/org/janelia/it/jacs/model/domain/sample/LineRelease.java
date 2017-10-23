package org.janelia.it.jacs.model.domain.sample;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.janelia.it.jacs.model.domain.AbstractDomainObject;
import org.janelia.it.jacs.model.domain.support.MongoMapped;

/**
 * A release definition which controls how Samples are released and published to the external website. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="release",label="Line Release")
public class LineRelease extends AbstractDomainObject {

	private boolean autoRelease;
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date releaseDate;
    private Integer lagTimeMonths;
    private boolean sageSync;    
    private List<String> dataSets = new ArrayList<>();
    private List<String> annotators = new ArrayList<>();
    private List<String> subscribers = new ArrayList<>();

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

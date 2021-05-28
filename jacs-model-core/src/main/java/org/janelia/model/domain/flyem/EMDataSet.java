package org.janelia.model.domain.flyem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

/**
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="emDataSet",label="EM Data Set")
@SearchType(key="emDataSet",label="EM Data Set")
public class EMDataSet extends AbstractDomainObject {

    @SearchAttribute(key="version_txt",label="Version")
    private String version;

    @SearchAttribute(key="published_b",label="Published")
    private boolean published;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @SearchAttribute(key="identifier_txt",label="Identifier")
    @JsonIgnore
    public String getDataSetIdentifier() {
        return getName()+":v"+getVersion();
    }
}

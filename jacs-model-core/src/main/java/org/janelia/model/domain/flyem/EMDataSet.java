package org.janelia.model.domain.flyem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.gui.search.Filtering;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.gui.search.criteria.FacetCriteria;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

import java.util.ArrayList;
import java.util.List;

/**
 * Data set loaded from FlyEM's neuPrint.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="emDataSet",label="EM Data Set")
@SearchType(key="emDataSet",label="EM Data Set")
public class EMDataSet extends AbstractDomainObject implements Filtering {

    @SearchAttribute(key="version_txt",label="Version")
    private String version;

    @SearchAttribute(key="gender_txt",label="Gender",facet="gender_s")
    private Boolean gender;

    @SearchAttribute(key="published_b",label="Is Published",facet="published_b")
    private boolean published;

    @SearchAttribute(key="active_b",label="Is Active",facet="active_b")
    private boolean active;

    @JsonIgnore
    private List<Criteria> lazyCriteria;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @SearchAttribute(key="identifier_txt",label="Identifier")
    @JsonIgnore
    public String getDataSetIdentifier() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        if (StringUtils.isNotBlank(getVersion())) {
            sb.append(":v");
            sb.append(getVersion());
        }
        return sb.toString();
    }

    /* implement Filtering interface */

    @JsonIgnore
    @Override
    public String getSearchClass() {
        return EMBody.class.getName();
    }

    @JsonIgnore
    @Override
    public boolean hasCriteria() {
        return true;
    }

    @JsonIgnore
    @Override
    public String getSearchString() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<Criteria> getCriteriaList() {
        if (lazyCriteria==null) {
            lazyCriteria = new ArrayList<>();
            FacetCriteria activeFlag = new FacetCriteria();
            activeFlag.setAttributeName("active");
            activeFlag.setValues(Sets.newHashSet("true"));
            lazyCriteria.add(activeFlag);
            FacetCriteria dataSet = new FacetCriteria();
            dataSet.setAttributeName("dataSetIdentifier");
            dataSet.getValues().add(getDataSetIdentifier());
            lazyCriteria.add(dataSet);
        }
        return lazyCriteria;
    }

}

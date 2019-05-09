package org.janelia.model.domain.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Preference;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ontology.OntologyTerm;

import java.util.List;

/**
 * Created by schauderd on 8/24/15.
 */
public class DomainQuery {

    private String subjectKey;
    private List<Reference> references;
    private List<Long> objectIds;
    private List<Integer> ordering;
    private String objectType;
    private String propertyName;
    private String propertyValue;
    private DomainObject domainObject;
    private List<OntologyTerm> objectList;
    private Preference preference;

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public String getSubjectKey() {
        return subjectKey;
    }

    public void setSubjectKey(String subjectKey) {
        this.subjectKey = subjectKey;
    }


    public List<Long> getObjectIds() {
        return objectIds;
    }

    public void setObjectIds(List<Long> objectIds) {
        this.objectIds = objectIds;
    }


    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }


    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public DomainObject getDomainObject() {
        return domainObject;
    }

    public void setDomainObject(DomainObject domainObject) {
        this.domainObject = domainObject;
    }

    @SuppressWarnings("unchecked")
    public <D extends DomainObject> D getDomainObjectAs(Class<D> domainObjectClass) {
        return (D) this.domainObject;
    }

    public List<Integer> getOrdering() {
        return ordering;
    }

    public void setOrdering(List<Integer> ordering) {
        this.ordering = ordering;
    }

    public List<OntologyTerm> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<OntologyTerm> objectList) {
        this.objectList = objectList;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("subjectKey", subjectKey)
                .append("references", references)
                .append("objectIds", objectIds)
                .append("ordering", ordering)
                .append("objectType", objectType)
                .append("propertyName", propertyName)
                .append("propertyValue", propertyValue)
                .append("domainObject", domainObject)
                .append("objectList", objectList)
                .append("preference", preference)
                .toString();
    }
}

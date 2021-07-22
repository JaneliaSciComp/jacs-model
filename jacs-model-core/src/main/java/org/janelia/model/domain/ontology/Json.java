package org.janelia.model.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A complex annotation that contains a JSON text value. The value is never shown to the user
 * and must be edited using a custom editor.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class Json extends Text {

    // Class used to deserialize the JSON value
    private String valueClass;

    public Json() {
    }

    public void init(String valueClass) {
        this.valueClass = valueClass;
    }

    public boolean allowsChildren() {
        return false;
    }

    @JsonIgnore
    public String getTypeName() {
        return "JSON";
    }

    /**
     * Never show the JSON to the user directly.
     */
    public String createAnnotationName(Annotation annotation) {
        return annotation.getKey();
    }

    public String getValueClass() {
        return valueClass;
    }

    public void setValueClass(String valueClass) {
        this.valueClass = valueClass;
    }
}

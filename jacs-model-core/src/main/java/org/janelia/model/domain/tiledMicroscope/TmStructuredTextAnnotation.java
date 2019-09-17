package org.janelia.model.domain.tiledMicroscope;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.protostuff.Tag;

/**
 * This class represents a structured text annotation to be attached to other tiled
 * microscope things, most typically TmGeoAnnotations.
 *
 * @author djo
 */
public class TmStructuredTextAnnotation implements Serializable {

    private Long id;

    /** The id of a geometric annotation */
    private Long parentId;

    /** An annotation in JSON format */
    private String dataString;
    
	/** No-args c'tor required for use with Protostuff/protobuf */
	public TmStructuredTextAnnotation() {		
	}

    public TmStructuredTextAnnotation(Long id, Long parentId, String dataString) {
        this.id = id;
        this.parentId = parentId;
        this.dataString = dataString;
    }

    /**
     * retrieve data, parsed; if we can't parse the stored string, return an empty object node instead
     */
    public JsonNode getData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(getDataString());
        }
        catch (IOException e) {
            e.printStackTrace();
            return mapper.createObjectNode();
        }
    }

    /**
     * update the data string in the annotation with data from a new JSON object; doesn't update
     * value on error
     */
    public void setData(JsonNode node) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            setDataString(mapper.writeValueAsString(node));
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    @Override
    public String toString() {
        return "TmStructuredTextAnnotation[id=" + id + ", parentId=" + parentId + ", dataString=" + dataString + "]";
    }

}


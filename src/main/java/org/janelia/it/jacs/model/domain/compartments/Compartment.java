package org.janelia.it.jacs.model.domain.compartments;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.it.jacs.model.domain.enums.FileType;
import org.janelia.it.jacs.model.domain.interfaces.HasFiles;
import org.janelia.it.jacs.model.domain.interfaces.HasIdentifier;

public class Compartment implements HasIdentifier, HasFiles, Serializable {

    private Long id;
    private String name;
    private String code;
    private String color;
    private Integer number;
    private Map<FileType, String> files = new HashMap<>();
    private transient CompartmentSet parent;

    @JsonIgnore
    public CompartmentSet getParent() {
        return parent;
    }

    @JsonIgnore
    void setParent(CompartmentSet parent) {
        this.parent = parent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Map<FileType, String> getFiles() {
        return files;
    }

    public void setFiles(Map<FileType, String> files) {
        if (files==null) throw new IllegalArgumentException("Property cannot be null");
        this.files = files;
    }
}

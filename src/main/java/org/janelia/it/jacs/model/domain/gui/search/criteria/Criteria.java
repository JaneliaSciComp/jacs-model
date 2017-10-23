package org.janelia.it.jacs.model.domain.gui.search.criteria;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class Criteria implements Serializable {

}

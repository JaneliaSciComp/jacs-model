package org.janelia.model.domain.sample;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.janelia.model.domain.enums.PipelineStatus;
import org.janelia.model.domain.support.MongoMapped;

/**
 * Stores information about pipeline status transitions
 * 
 * @author <a href="mailto:schauderd@janelia.hhmi.org">David Schauder</a>
 */
@MongoMapped(collectionName="pipelineStatus",label="StatusTransition")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class StatusTransition implements Serializable {
    private Long sampleId;
    private String orderNo;
    private PipelineStatus source;
    private PipelineStatus target;
    private String process;
    private Date transitionDate;
    private Map<String, Object> parameters = new HashMap<>();


    public PipelineStatus getSource() {
        return source;
    }

    public void setSource(PipelineStatus source) {
        this.source = source;
    }

    public PipelineStatus getTarget() {
        return target;
    }

    public void setTarget(PipelineStatus target) {
        this.target = target;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public Date getTransitionDate() {
        return transitionDate;
    }

    public void setTransitionDate(Date transitionDate) {
        this.transitionDate = transitionDate;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }


    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getSampleId() {
        return sampleId;
    }

    public void setSampleId(Long sampleId) {
        this.sampleId = sampleId;
    }


}

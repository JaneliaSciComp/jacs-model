package org.janelia.model.domain.tiledMicroscope;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.ReverseReference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;
import org.janelia.model.domain.support.SearchType;
import org.janelia.model.security.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Workspace for annotating a sample in Horta.
 *
 * Always linked to imagery (TmSample). May contain neuron fragments (TmNeuronMetadata) and may be linked to a
 * collection of published neurons (TmMappedNeuron).
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="tmWorkspace",label="Horta Workspace")
@MongoMapped(collectionName="tmWorkspace",label="Horta Workspace")
public class TmWorkspace extends AbstractDomainObject {

    private Reference sampleRef;

    @SearchTraversal({TmWorkspace.class})
    private ReverseReference mappedNeurons;

    private boolean autoTracing;
    private boolean autoPointRefinement;
    private boolean containsFragments;

    @SearchAttribute(key="swcpath_txt",label="Original SWC Path")
    private String originalSWCPath;
    
    private TmColorModel colorModel;
    private TmColorModel colorModel3d;
    private String neuronCollection;
    private List<TmObjectMesh> objectMeshList;
    private String tracingGroup;

    public TmWorkspace() {
        containsFragments = false;
    }

    public TmWorkspace(String name, Long sampleID) {
        setName(name);
        containsFragments = false;
        this.sampleRef = Reference.createFor("TmSample", sampleID);
    }
    
    public static TmWorkspace copy(TmWorkspace workspace) {
        TmWorkspace copy = new TmWorkspace();
        copy.setAutoPointRefinement(workspace.isAutoPointRefinement());
        copy.setAutoTracing(workspace.isAutoTracing());
        copy.setColorModel(workspace.getColorModel());
        copy.setColorModel3d(workspace.getColorModel3d());
        copy.setSampleRef(workspace.getSampleRef());
        copy.setMappedNeurons(workspace.getMappedNeurons());
        return copy;
    }
    
    @SearchAttribute(key="sample_id_l",label="Sample GUID")
    @JsonIgnore
    public Long getSampleId() {
        return sampleRef==null?null:sampleRef.getTargetId();
    }
    
    public Reference getSampleRef() {
        return sampleRef;
    }

    public void setSampleRef(Reference sampleRef) {
        this.sampleRef = sampleRef;
    }

    public ReverseReference getMappedNeurons() {
        return mappedNeurons;
    }

    public void setMappedNeurons(ReverseReference mappedNeurons) {
        this.mappedNeurons = mappedNeurons;
    }

    public void setObjectMeshList(List<TmObjectMesh> objectMeshList) {
        this.objectMeshList = objectMeshList;
    }

    public boolean isAutoTracing() {
        return autoTracing;
    }

    public void setAutoTracing(boolean autoTracing) {
        this.autoTracing = autoTracing;
    }

    public boolean isAutoPointRefinement() {
        return autoPointRefinement;
    }

    public void setAutoPointRefinement(boolean autoPointRefinement) {
        this.autoPointRefinement = autoPointRefinement;
    }

    public String getOriginalSWCPath() {
        return originalSWCPath;
    }

    public void setOriginalSWCPath(String originalSWCPath) {
        this.originalSWCPath = originalSWCPath;
    }

    public TmColorModel getColorModel() {
        return colorModel;
    }

    public void setColorModel(TmColorModel colorModel) {
        this.colorModel = colorModel;
    }

    public TmColorModel getColorModel3d() {
        return colorModel3d;
    }

    public void setColorModel3d(TmColorModel colorModel3d) {
        this.colorModel3d = colorModel3d;
    }

    public void addObjectMesh (TmObjectMesh objectMesh) {
        if (objectMeshList==null)
            objectMeshList = new ArrayList<TmObjectMesh>();
        objectMeshList.add(objectMesh);
    }

    public void removeObjectMesh (TmObjectMesh objectMesh) {
        if (objectMeshList==null)
            objectMeshList = new ArrayList<TmObjectMesh>();
        objectMeshList.remove(objectMesh);
    }

    public List<TmObjectMesh> getObjectMeshList() {
        return objectMeshList;
    }

    public void setObjectMesh(List<TmObjectMesh> objectMeshList) {
        this.objectMeshList = objectMeshList;
    }

    public TmWorkspace rename(String newName) {
        this.setName(newName);
        return this;
    }

    public boolean isContainsFragments() {
        return containsFragments;
    }

    public void setContainsFragments(boolean containsFragments) {
        this.containsFragments = containsFragments;
    }

    public String getTracingGroup() {
        return tracingGroup;
    }

    public void setTracingGroup(String tracingGroupKey) {
        this.tracingGroup = tracingGroupKey;
    }

    public String getNeuronCollection() {
        return neuronCollection;
    }

    public void setNeuronCollection(String neuronCollection) {
        this.neuronCollection = neuronCollection;
    }
}

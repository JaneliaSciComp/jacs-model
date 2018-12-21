package org.janelia.model.domain.tiledMicroscope;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

/**
 * RawTile microscope workspace for annotating a TmSample.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="tmWorkspace",label="Tiled Microscope Workspace")
@MongoMapped(collectionName="tmWorkspace",label="Tiled Microscope Workspace")
public class TmWorkspace extends AbstractDomainObject {

    private Reference sampleRef;

    private boolean autoTracing;
    private boolean autoPointRefinement;

    @SearchAttribute(key="swcpath_txt",label="Original SWC Path")
    private String originalSWCPath;
    
    private TmColorModel colorModel;
    private TmColorModel colorModel3d;
    private List<TmObjectMesh> objectMeshList;

    public TmWorkspace() {
    }

    public TmWorkspace(String name, Long sampleID) {
        setName(name);
        this.sampleRef = Reference.createFor("TmSample", sampleID);
    }
    
    public static TmWorkspace copy(TmWorkspace workspace) {
        TmWorkspace copy = new TmWorkspace();
        copy.setAutoPointRefinement(workspace.isAutoPointRefinement());
        copy.setAutoTracing(workspace.isAutoTracing());
        copy.setColorModel(workspace.getColorModel());
        copy.setColorModel3d(workspace.getColorModel3d());
        copy.setSampleRef(workspace.getSampleRef());
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
}

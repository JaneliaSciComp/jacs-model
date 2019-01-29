package org.janelia.model.domain.tiledMicroscope;

public class TmObjectMesh {
    // need to add reference mapping for allen brain transformation
    private String pathToObjFile;
    private String name;


    public String getPathToObjFile() {
        return pathToObjFile;
    }

    public void setPathToObjFile(String pathToObjFile) {
        this.pathToObjFile = pathToObjFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public TmObjectMesh() {
    }

    public TmObjectMesh(String name, String pathToObjFile) {
        setName(name);
        setPathToObjFile(pathToObjFile);
    }

}


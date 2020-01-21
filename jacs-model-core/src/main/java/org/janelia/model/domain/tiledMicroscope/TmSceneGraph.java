package org.janelia.model.domain.tiledMicroscope;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a placeholder class for all future mesh references in a workspace.  There may be all kinds of object meshes
 * in a workspace that need positional and relationship information persisted between workstation sessions.
 * Although the implementation of this will start out simplistic, the idea would be to build on the many years
 * of excellent scenegraph implementations in frameworks like JME, Unity, or OSG.
 */
public class TmSceneGraph {
    Map<String, Object> meshInfoMap;

    public Map<String, Object> getMeshes() {
        return meshInfoMap;
    }

    public void setMeshObject (String key, Object meshInfo) {
        meshInfoMap.put(key, meshInfo);
    }

    public void removeMeshObject (String key) {
        meshInfoMap.remove(key);
    }

    public Object getMeshObject (String key) {
        return meshInfoMap.get(key);
    }
}

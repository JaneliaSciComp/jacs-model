package org.janelia.model.domain.files;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.interfaces.IsParent;
import org.janelia.model.domain.support.SearchType;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a synchronized path to a top-level folder that is periodically searched for data sets
 * (n5 containers, zarr containers, TM samples, etc.)
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="syncedRoot",label="Synchronized Folder")
public class SyncedRoot extends SyncedPath implements IsParent {

    private Set<Class<? extends SyncedPath>> syncClasses = new HashSet<>();

    public Set<Class<? extends SyncedPath>> getSyncClasses() {
        return syncClasses;
    }

    public void setSyncClasses(Set<Class<? extends SyncedPath>> syncClasses) {
        if (syncClasses==null) throw new IllegalArgumentException("Property cannot be null");
        this.syncClasses = syncClasses;
    }

    @JsonIgnore
    public void addSyncClass(Class<? extends SyncedPath>syncClass) {
        syncClasses.add(syncClass);
    }

    @JsonIgnore
    public void removeSyncClass(Class<? extends SyncedPath>syncClass) {
        syncClasses.remove(syncClass);
    }
}

package org.janelia.model.domain.files;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.DomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchType;

/**
 * A path which is periodically synchronized from disk to maintain a database representation.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@SearchType(key="syncedPath",label="Synchronized Path")
@MongoMapped(collectionName="syncedPath",label="Synchronized Path")
public class SyncedPath extends AbstractDomainObject implements HasSyncStorage {

    /** Root path where this path was discovered */
    private Reference rootRef;

    @SearchAttribute(key="filepath_txt",label="Filepath")
    private String filepath;

    /** This flag is set to false when a desync is detected, meaning that the filepath cannot be found in storage */
    @SearchAttribute(key="exists_b",label="Exists in Storage")
    private boolean existsInStorage = true;

    public Reference getRootRef() {
        return rootRef;
    }

    public void setRootRef(Reference rootRef) {
        this.rootRef = rootRef;
    }

    @Override
    public String getFilepath() {
        return filepath;
    }

    @Override
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public boolean isExistsInStorage() {
        return existsInStorage;
    }

    @Override
    public void setExistsInStorage(boolean existsInStorage) {
        this.existsInStorage = existsInStorage;
    }
}

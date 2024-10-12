package org.janelia.model.domain.files;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.support.SearchAttribute;

/**
 * A path which is periodically synchronized from disk to maintain a database representation.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SyncedPath extends AbstractDomainObject implements HasSyncStorage {

    @SearchAttribute(key="filepath_txt",label="Filepath")
    private String filepath;

    /** This flag is set to false when a desync is detected, meaning that the filepath cannot be found in storage */
    @SearchAttribute(key="exists_b",label="Exists in Storage")
    private boolean existsInStorage = true;

    /** True if this object should be synchronized automatically, e.g. by the SyncedRootProcessor */
    private boolean autoSynchronized = false;

    private Map<String, Object> storageAttributes = new HashMap<>();

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

    public boolean isAutoSynchronized() {
        return autoSynchronized;
    }

    public void setAutoSynchronized(boolean autoSynchronize) {
        this.autoSynchronized = autoSynchronize;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, Object> getStorageAttributes() {
        return Collections.unmodifiableMap(storageAttributes);
    }

    public void setStorageAttributes(Map<String, Object> storageAttributes) {
        if (storageAttributes != null) {
            this.storageAttributes.putAll(storageAttributes);
        }
    }

    public void setStorageAttribute(String attrName, Object attrValue) {
        if (attrValue != null) {
            storageAttributes.put(attrName, attrValue);
        } else {
            storageAttributes.remove(attrName);
        }
    }

}

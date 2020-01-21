package org.janelia.model.domain.tiledMicroscope;


import com.fasterxml.jackson.annotation.JsonIgnore;

public class AsyncPersistence {

    @JsonIgnore
    transient private boolean synced;
    @JsonIgnore
    transient private int syncLevel = 0;
    private Boolean largeNeuron;


    @JsonIgnore
    public int getSyncLevel() {
        return syncLevel;
    }

    public synchronized void decrementSyncLevel() {
        this.syncLevel--;
    }

    public synchronized void incrementSyncLevel() {
        this.syncLevel++;
    }

    @JsonIgnore
    public boolean isSynced() {
        return synced;
    }

    @JsonIgnore
    public void setSynced(boolean synced) {
        this.synced = synced;
    }


    public Boolean isLargeNeuron() {
        return largeNeuron;
    }

    public void setLargeNeuron(Boolean largeNeuron) {
        this.largeNeuron = largeNeuron;
    }

}

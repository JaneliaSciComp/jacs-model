package org.janelia.model.domain.tiledMicroscope;

import java.util.Date;

public class TmWorkspaceInfo {
    private Long workspaceId;
    private String workspaceName;
    private Long totalSize;
    private String ownerKey;
    private Date dateCreated;

    public TmWorkspaceInfo(Long workspaceId, String workspaceName, Long totalSize, String ownerKey, Date dateCreated) {
        this.workspaceId = workspaceId;
        this.workspaceName = workspaceName;
        this.totalSize = totalSize;
        this.ownerKey = ownerKey;
        this.dateCreated = dateCreated;
    }

    public String getWorkspaceName() {
        return workspaceName;
    }

    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(Long totalSize) {
        this.totalSize = totalSize;
    }

    public String getOwnerKey() {
        return ownerKey;
    }

    public void setOwnerKey(String ownerKey) {
        this.ownerKey = ownerKey;
    }
}



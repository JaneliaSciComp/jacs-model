package org.janelia.model.access.domain.dao.searchables;

import java.util.List;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.WorkspaceNodeDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.workspace.Workspace;

/**
 * {@link Workspace} DAO.
 */
@AsyncIndex
public class WorkspaceNodeSearchableDao extends AbstractNodeSearchableDao<Workspace> implements WorkspaceNodeDao {

    private final WorkspaceNodeDao workspaceNodeDao;

    @Inject
    WorkspaceNodeSearchableDao(WorkspaceNodeDao workspaceNodeDao,
                               @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(workspaceNodeDao, objectIndexer);
        this.workspaceNodeDao = workspaceNodeDao;
    }

    @Override
    public Workspace createDefaultWorkspace(String subjectKey) {
        Workspace persistedWorkspace = workspaceNodeDao.createDefaultWorkspace(subjectKey);
        domainObjectIndexer.indexDocument(persistedWorkspace);
        return persistedWorkspace;
    }

    @Override
    public List<Workspace> getWorkspaceNodesAccessibleBySubjectGroups(String subjectKey, long offset, int length) {
        return workspaceNodeDao.getWorkspaceNodesAccessibleBySubjectGroups(subjectKey, offset, length);
    }

    @Override
    public List<Workspace> getWorkspaceNodesOwnedBySubjectKey(String subjectKey) {
        return workspaceNodeDao.getWorkspaceNodesOwnedBySubjectKey(subjectKey);
    }

    @Override
    public Workspace getDefaultWorkspace(String subjectKey) {
        return workspaceNodeDao.getDefaultWorkspace(subjectKey);
    }

    @Override
    public List<Workspace> getNodeDirectAncestors(Reference nodeReference) {
        return workspaceNodeDao.getNodeDirectAncestors(nodeReference);
    }

    @Override
    public List<Workspace> getNodesByParentNameAndOwnerKey(Long parentNodeId, String name, String ownerKey) {
        return workspaceNodeDao.getNodesByParentNameAndOwnerKey(parentNodeId, name, ownerKey);
    }
}

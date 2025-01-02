package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.access.domain.dao.NDContainerDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.files.NDContainer;

import jakarta.inject.Inject;

@AsyncIndex
public class NDContainerSearchableDao extends AbstractDomainSearchableDao<NDContainer> implements NDContainerDao {

    private final NDContainerDao ndContainerDao;

    @Inject
    public NDContainerSearchableDao(NDContainerDao ndContainerDao,
                                    DomainObjectDao<NDContainer> domainObjectDao,
                                    @AsyncIndex DomainObjectIndexer domainObjectIndexer) {
        super(domainObjectDao, domainObjectIndexer);
        this.ndContainerDao = ndContainerDao;
    }
}

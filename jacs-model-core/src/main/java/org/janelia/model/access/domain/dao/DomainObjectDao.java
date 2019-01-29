package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.DomainObject;

/**
 * Base interface for domain object access.
 *
 * @param <T> entity type
 */
public interface DomainObjectDao<T extends DomainObject> extends DomainObjectReadDao<T>, DomainObjectWriteDao<T> {
}

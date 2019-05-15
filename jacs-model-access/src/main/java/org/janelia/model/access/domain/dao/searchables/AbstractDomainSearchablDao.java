package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Abstract Searchable DAO.
 *
 * @param <T> type of the element
 */
public abstract class AbstractDomainSearchablDao<T extends DomainObject>
        implements DomainObjectDao<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDomainSearchablDao.class);

    final DomainObjectDao<T> domainObjectDao;
    final DomainObjectIndexer domainObjectIndexer;

    AbstractDomainSearchablDao(DomainObjectDao<T> domainObjectDao,
                               DomainObjectIndexer domainObjectIndexer) {
        this.domainObjectDao = domainObjectDao;
        this.domainObjectIndexer = domainObjectIndexer;
    }

    @Override
    public T findById(Long id) {
        return domainObjectDao.findById(id);
    }

    @Override
    public List<T> findByIds(Collection<Long> ids) {
        return domainObjectDao.findByIds(ids);
    }

    @Override
    public List<T> findAll(long offset, int length) {
        return domainObjectDao.findAll(offset, length);
    }

    @Override
    public T findEntityByIdAccessibleBySubjectKey(Long id, String subjectKey) {
        return domainObjectDao.findEntityByIdAccessibleBySubjectKey(id, subjectKey);
    }

    @Override
    public List<T> findEntitiesByIdsAccessibleBySubjectKey(List<Long> ids, String subjectKey) {
        return domainObjectDao.findEntitiesByIdsAccessibleBySubjectKey(ids, subjectKey);
    }

    @Override
    public List<T> findOwnedEntitiesBySubjectKey(String subjectKey, long offset, int length) {
        return domainObjectDao.findOwnedEntitiesBySubjectKey(subjectKey, offset, length);
    }

    @Override
    public List<T> findEntitiesReadableBySubjectKey(@Nullable String subjectKey, long offset, int length) {
        return domainObjectDao.findEntitiesReadableBySubjectKey(subjectKey, offset, length);
    }

    @Override
    public long deleteByIdAndSubjectKey(Long id, String subjectKey) {
        long n = domainObjectDao.deleteByIdAndSubjectKey(id, subjectKey);
        if (n > 0) {
            domainObjectIndexer.removeDocument(id);
        }
        return n;
    }

    @Override
    public T saveBySubjectKey(T entity, String subjectKey) {
        T persistedEntity = domainObjectDao.saveBySubjectKey(entity, subjectKey);
        domainObjectIndexer.indexDocument(persistedEntity);
        return persistedEntity;
    }

    @Override
    public void save(T entity) {
        domainObjectDao.save(entity);
        domainObjectIndexer.indexDocument(entity);
    }

    @Override
    public void saveAll(Collection<T> entities) {
        domainObjectDao.saveAll(entities);
        domainObjectIndexer.indexDocumentStream(entities.stream().map(e -> (DomainObject)e));
    }

    @Override
    public void delete(T entity) {
        domainObjectDao.delete(entity);
        domainObjectIndexer.removeDocument(entity.getId());
    }

    @Override
    public DaoUpdateResult update(Long entityId, Map<String, EntityFieldValueHandler<?>> fieldsToUpdate) {
        DaoUpdateResult updateResult = domainObjectDao.update(entityId, fieldsToUpdate);
        if (updateResult.getEntitiesAffected() > 0) {
            try {
                T entityRef = getEntityType().newInstance();
                domainObjectIndexer.indexDocument(entityRef);
            } catch (Exception e) {
                LOG.error("Error creating a reference to {} of type {} for indexing", entityId, getEntityType(), e);
            }
        }
        return updateResult;
    }
}

package org.janelia.model.access.domain.dao.searchables;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.janelia.model.access.domain.dao.DaoUpdateResult;
import org.janelia.model.access.domain.dao.DomainObjectDao;
import org.janelia.model.access.domain.dao.EntityFieldValueHandler;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.DomainObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Searchable DAO.
 *
 * @param <T> type of the element
 */
public abstract class AbstractDomainSearchableDao<T extends DomainObject> implements DomainObjectDao<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractDomainSearchableDao.class);

    final DomainObjectDao<T> domainObjectDao;
    final DomainObjectIndexer domainObjectIndexer;

    AbstractDomainSearchableDao(DomainObjectDao<T> domainObjectDao,
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
    public T findEntityByIdReadableBySubjectKey(Long id, String subjectKey) {
        return domainObjectDao.findEntityByIdReadableBySubjectKey(id, subjectKey);
    }

    @Override
    public List<T> findEntitiesByIdsReadableBySubjectKey(List<Long> ids, String subjectKey) {
        return domainObjectDao.findEntitiesByIdsReadableBySubjectKey(ids, subjectKey);
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
    public List<T> findEntitiesByExactName(String name) {
        return domainObjectDao.findEntitiesByExactName(name);
    }

    @Override
    public List<T> findEntitiesWithMatchingName(String name) {
        return domainObjectDao.findEntitiesWithMatchingName(name);
    }

    @Override
    public Stream<T> streamAll() {
        return domainObjectDao.streamAll();
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

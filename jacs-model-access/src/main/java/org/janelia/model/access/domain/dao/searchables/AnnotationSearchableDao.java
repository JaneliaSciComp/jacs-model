package org.janelia.model.access.domain.dao.searchables;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.AnnotationDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.ontology.Annotation;

import javax.inject.Inject;

/**
 * {@link Annotation} DAO.
 */
@AsyncIndex
public class AnnotationSearchableDao extends AbstractDomainSearchablDao<Annotation> implements AnnotationDao {

    private final AnnotationDao annotationDao;

    @Inject
    AnnotationSearchableDao(AnnotationDao annotationDao,
                            @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(annotationDao, objectIndexer);
        this.annotationDao = annotationDao;
    }

}

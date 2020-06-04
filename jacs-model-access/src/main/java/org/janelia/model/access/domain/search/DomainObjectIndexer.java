package org.janelia.model.access.domain.search;

import java.util.Set;
import java.util.stream.Stream;

import org.janelia.model.domain.DomainObject;

public interface  DomainObjectIndexer {
    DocumentSearchResults searchIndex(DocumentSearchParams searchParams);
    boolean indexDocument(DomainObject domainObject);
    boolean removeDocument(Long docId);
    int indexDocumentStream(Stream<? extends DomainObject> domainObjectStream);
    int removeDocumentStream(Stream<Long> docIdsStream);
    void removeIndex();
    void updateDocsAncestors(Set<Long> docIds, Long ancestorId);
}

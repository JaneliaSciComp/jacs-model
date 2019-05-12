package org.janelia.model.access.domain.search;

import org.janelia.model.domain.DomainObject;

import java.util.Set;
import java.util.stream.Stream;

public interface  DomainObjectIndexer {
    DocumentSearchResults searchIndex(DocumentSearchParams searchParams);
    boolean indexDocument(DomainObject domainObject);
    boolean indexDocumentStream(Stream<DomainObject> domainObjectStream);
    boolean removeDocument(Long docId);
    boolean removeDocumentStream(Stream<Long> docIdsStream);
    void removeIndex();
    void updateDocsAncestors(Set<Long> docIds, Long ancestorId);
}

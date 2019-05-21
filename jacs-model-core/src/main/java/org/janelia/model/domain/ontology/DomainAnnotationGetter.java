package org.janelia.model.domain.ontology;

import java.util.Set;

import org.janelia.model.domain.Reference;

public interface DomainAnnotationGetter {
    Set<SimpleDomainAnnotation> getAnnotations(Reference domainReference);
}

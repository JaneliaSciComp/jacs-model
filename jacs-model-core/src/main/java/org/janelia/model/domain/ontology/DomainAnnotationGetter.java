package org.janelia.model.domain.ontology;

import java.util.Set;

import org.janelia.model.domain.DataSupplier;
import org.janelia.model.domain.Reference;

public interface DomainAnnotationGetter extends DataSupplier<Reference, Set<SimpleDomainAnnotation>> {
    Set<SimpleDomainAnnotation> getAnnotations(Reference domainReference);

    @Override
    default Set<SimpleDomainAnnotation> getData(Reference key) {
        return getAnnotations(key);
    }
}

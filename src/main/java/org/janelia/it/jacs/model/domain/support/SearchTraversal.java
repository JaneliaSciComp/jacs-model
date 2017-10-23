package org.janelia.it.jacs.model.domain.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking a field as okay to traverse when indexing from some set of root domain objects, 
 * given by the classes in the annotation value. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SearchTraversal {
    Class<?>[] value();
}

package org.janelia.it.jacs.model.domain.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for mapping a class hierarchy to a given collection in MongoDB. 
 * Only the top-level class should be mapped using this annotation, since Jongo 
 * will instantiate the appropriate subclass by using JsonTypeInfo. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MongoMapped {
    String collectionName();
    String label();
}

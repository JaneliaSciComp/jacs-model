package org.janelia.model.domain.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for triggering reprocessing upon change in SAGE
 * 
 * @author <a href="mailto:schauderd@janelia.hhmi.org">David Schauder</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD})
public @interface ReprocessOnChange {
}

package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.sample.Image;

/**
 * ImageDao data access object
 * @param <T> actual image type
 */
public interface ImageDao<T extends Image> extends DomainObjectDao<T> {
}

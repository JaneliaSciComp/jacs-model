package org.janelia.model.access.domain.dao;

import org.janelia.model.domain.sample.PublishedImage;

import java.util.Map;

/**
 * data access object for PublishedImage
 */
public interface PublishedImageDao extends DomainObjectDao<PublishedImage> {
    PublishedImage getImage(String slideCode, String alignmentSpace, String objective);
}

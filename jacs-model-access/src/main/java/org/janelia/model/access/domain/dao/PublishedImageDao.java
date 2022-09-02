package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;

import org.janelia.model.domain.sample.PublishedImage;

/**
 * data access object for PublishedImage
 */
public interface PublishedImageDao extends DomainObjectDao<PublishedImage> {
    List<PublishedImage> getImages(String alignmentSpace, Collection<String> slideCodes, String objective);
    List<PublishedImage> getGen1Gal4LexAImages(String anatomicalArea, Collection<String> lines);
}

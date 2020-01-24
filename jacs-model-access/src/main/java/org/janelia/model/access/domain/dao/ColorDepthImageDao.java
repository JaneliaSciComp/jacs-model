package org.janelia.model.access.domain.dao;

import java.util.List;
import java.util.stream.Stream;

import org.janelia.model.domain.gui.cdmip.ColorDepthImage;

/**
 * Dataset data access object
 */
public interface ColorDepthImageDao extends DomainObjectDao<ColorDepthImage> {
    long countColorDepthMIPs(String ownerKey, String alignmentSpace, List<String> libraryNames, List<String> matchingNames, List<String> matchingFilepaths);
    Stream<ColorDepthImage> streamColorDepthMIPs(String ownerKey, String alignmentSpace, List<String> libraryNames, List<String> matchingNames, List<String> matchingFilepaths, int offset, int length);
    void updatePublicUrls(ColorDepthImage cdmi);
}

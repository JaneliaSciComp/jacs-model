package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;

/**
 * Dataset data access object
 */
public interface ColorDepthImageDao extends DomainObjectDao<ColorDepthImage> {
    long countColorDepthMIPs(String ownerKey, String alignmentSpace,
                             Collection<String> libraryNames,
                             Collection<String> matchingNames,
                             Collection<String> matchingFilepaths,
                             Collection<String> matchingSampleRefs);
    Map<String, Integer> countColorDepthMIPsByAlignmentSpaceForLibrary(String library);
    Stream<ColorDepthImage> streamColorDepthMIPs(String ownerKey, String alignmentSpace,
                                                 Collection<String> libraryNames,
                                                 Collection<String> matchingNames,
                                                 Collection<String> matchingFilepaths,
                                                 Collection<String> matchingSampleRefs,
                                                 int offset, int length);
    void updatePublicUrls(ColorDepthImage cdmi);
    long addLibraryBySampleRefs(String libraryIdentifier, Collection<Reference> sampleRefs);
}

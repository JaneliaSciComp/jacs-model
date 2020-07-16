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
    long countColorDepthMIPs(ColorDepthImageQuery cdmQuery);
    Map<String, Integer> countColorDepthMIPsByAlignmentSpaceForLibrary(String library);
    Stream<ColorDepthImage> streamColorDepthMIPs(ColorDepthImageQuery cdmQuery);
    void updatePublicUrls(ColorDepthImage cdmi);
    long addLibraryBySampleRefs(String libraryIdentifier,
                                String objective,
                                Collection<Reference> sampleRefs,
                                boolean includeDerivedImages);
    long removeAllMipsFromLibrary(String libraryIdentifier);
}

package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibrary;

/**
 * Dataset data access object
 */
public interface ColorDepthImageDao extends DomainObjectDao<ColorDepthImage> {
    long countColorDepthMIPs(ColorDepthImageQuery cdmQuery);
    Map<String, Integer> countColorDepthMIPsByAlignmentSpaceForLibrary(String library);
    List<ColorDepthLibrary> countColorDepthMIPsByAlignmentSpaceForAllLibraries();
    Optional<ColorDepthImage> findColorDepthImageByPath(String imagePath);
    List<ColorDepthImage> findColorDepthMIPs(ColorDepthImageQuery cdmQuery);
    Stream<ColorDepthImage> streamColorDepthMIPs(ColorDepthImageQuery cdmQuery);
    void updatePublicUrls(ColorDepthImage cdmi);
    long addLibraryBySampleRefs(String libraryIdentifier,
                                String objective,
                                Collection<Reference> sampleRefs,
                                boolean includeDerivedImages);
    long removeAllMipsFromLibrary(String libraryIdentifier);
}

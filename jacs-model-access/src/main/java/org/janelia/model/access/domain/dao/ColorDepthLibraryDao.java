package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;

import org.janelia.model.domain.gui.cdmip.ColorDepthLibrary;

/**
 * Dataset data access object
 */
public interface ColorDepthLibraryDao extends DomainObjectDao<ColorDepthLibrary> {
    List<ColorDepthLibrary> getLibraryWithVariants(String libraryIdentifier);
    List<ColorDepthLibrary> getLibrariesByLibraryIdentifiers(Collection<String> libraryIdentifiers);
    void updateColorDepthCounts(List<ColorDepthLibrary> libraries);
}

package org.janelia.model.access.domain.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibrary;

/**
 * Dataset data access object
 */
public interface ColorDepthLibraryDao extends DomainObjectDao<ColorDepthLibrary> {
    List<ColorDepthLibrary> getLibraryWithVariants(String libraryIdentifier);
}

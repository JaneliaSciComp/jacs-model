package org.janelia.model.access.domain.dao.searchables;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.ColorDepthLibraryDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.gui.cdmip.ColorDepthLibrary;

/**
 * {@link ColorDepthLibrary} DAO.
 */
@AsyncIndex
public class ColorDepthLibrarySearchableDao extends AbstractDomainSearchableDao<ColorDepthLibrary> implements ColorDepthLibraryDao {

    private final ColorDepthLibraryDao colorDepthLibraryDao;

    @Inject
    ColorDepthLibrarySearchableDao(ColorDepthLibraryDao colorDepthLibraryDao,
                                   @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(colorDepthLibraryDao, objectIndexer);
        this.colorDepthLibraryDao = colorDepthLibraryDao;
    }

    @Override
    public List<ColorDepthLibrary> getLibraryWithVariants(String libraryIdentifier) {
        return colorDepthLibraryDao.getLibraryWithVariants(libraryIdentifier);
    }

    @Override
    public List<ColorDepthLibrary> getLibrariesByLibraryIdentifiers(Collection<String> libraryIdentifiers) {
        return colorDepthLibraryDao.getLibrariesByLibraryIdentifiers(libraryIdentifiers);
    }

    @Override
    public void updateColorDepthCounts(List<ColorDepthLibrary> libraries) {
        colorDepthLibraryDao.updateColorDepthCounts(libraries);
        domainObjectIndexer.indexDocumentStream(getLibrariesByLibraryIdentifiers(libraries.stream().map(ColorDepthLibrary::getIdentifier).collect(Collectors.toSet())).stream());
    }
}

package org.janelia.model.access.domain.dao.searchables;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.ColorDepthImageDao;
import org.janelia.model.access.domain.dao.ColorDepthImageQuery;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;

/**
 * {@link ColorDepthImage} DAO.
 */
@AsyncIndex
public class ColorDepthImageSearchableDao extends AbstractDomainSearchableDao<ColorDepthImage> implements ColorDepthImageDao {

    private final ColorDepthImageDao colorDepthImageDao;

    @Inject
    ColorDepthImageSearchableDao(ColorDepthImageDao colorDepthImageDao,
                                 @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(colorDepthImageDao, objectIndexer);
        this.colorDepthImageDao = colorDepthImageDao;
    }

    @Override
    public long countColorDepthMIPs(ColorDepthImageQuery cdmQuery) {
        return colorDepthImageDao.countColorDepthMIPs(cdmQuery);
    }

    @Override
    public Map<String, Integer> countColorDepthMIPsByAlignmentSpaceForLibrary(String library) {
        return colorDepthImageDao.countColorDepthMIPsByAlignmentSpaceForLibrary(library);
    }

    @Override
    public Stream<ColorDepthImage> streamColorDepthMIPs(ColorDepthImageQuery cdmQuery) {
        return colorDepthImageDao.streamColorDepthMIPs(cdmQuery);
    }

    @Override
    public void updatePublicUrls(ColorDepthImage cdmi) {
        colorDepthImageDao.updatePublicUrls(cdmi);
    }

    @Override
    public long addLibraryBySampleRefs(String libraryIdentifier, String objective, Collection<Reference> sampleRefs, boolean includeDerivedImages) {
        long nUpdates = colorDepthImageDao.addLibraryBySampleRefs(libraryIdentifier, objective, sampleRefs, includeDerivedImages);
        if (nUpdates > 0) {
            Set<String> ssampleRefs = sampleRefs.stream().map(Reference::toString).collect(Collectors.toSet());
            domainObjectIndexer.indexDocumentStream(colorDepthImageDao.streamColorDepthMIPs(
                    new ColorDepthImageQuery()
                            .withLibraryIdentifiers(Collections.singleton(libraryIdentifier))
                            .withSampleRefs(ssampleRefs)));
        }
        return nUpdates;
    }

    @Override
    public long removeAllMipsFromLibrary(String libraryIdentifier) {
        return colorDepthImageDao.removeAllMipsFromLibrary(libraryIdentifier);
    }
}

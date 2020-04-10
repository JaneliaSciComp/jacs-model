package org.janelia.model.access.domain.dao.searchables;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.ColorDepthImageDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.cdmip.ColorDepthImage;

/**
 * {@link ColorDepthImage} DAO.
 */
@AsyncIndex
public class ColorDepthImageSearchableDao extends AbstractDomainSearchablDao<ColorDepthImage> implements ColorDepthImageDao {

    private final ColorDepthImageDao colorDepthImageDao;

    @Inject
    ColorDepthImageSearchableDao(ColorDepthImageDao colorDepthImageDao,
                                 @AsyncIndex DomainObjectIndexer objectIndexer) {
        super(colorDepthImageDao, objectIndexer);
        this.colorDepthImageDao = colorDepthImageDao;
    }

    @Override
    public long countColorDepthMIPs(String ownerKey, String alignmentSpace,
                                    Collection<String> libraryNames,
                                    Collection<String> matchingNames,
                                    Collection<String> matchingFilepaths,
                                    Collection<String> matchingSampleRefs) {
        return colorDepthImageDao.countColorDepthMIPs(ownerKey, alignmentSpace, libraryNames, matchingNames, matchingFilepaths, matchingSampleRefs);
    }

    @Override
    public Map<String, Integer> countColorDepthMIPsByAlignmentSpaceForLibrary(String library) {
        return colorDepthImageDao.countColorDepthMIPsByAlignmentSpaceForLibrary(library);
    }

    @Override
    public Stream<ColorDepthImage> streamColorDepthMIPs(String ownerKey, String alignmentSpace,
                                                        Collection<String> libraryNames,
                                                        Collection<String> matchingNames,
                                                        Collection<String> matchingFilepaths,
                                                        Collection<String> matchingSampleRefs,
                                                        int offset, int length) {
        return colorDepthImageDao.streamColorDepthMIPs(ownerKey, alignmentSpace, libraryNames, matchingNames, matchingFilepaths, matchingSampleRefs, offset, length);
    }

    @Override
    public void updatePublicUrls(ColorDepthImage cdmi) {
        colorDepthImageDao.updatePublicUrls(cdmi);
    }

    @Override
    public long addLibraryBySampleRefs(String libraryIdentifier, Collection<Reference> sampleRefs) {
        return colorDepthImageDao.addLibraryBySampleRefs(libraryIdentifier, sampleRefs);
    }
}

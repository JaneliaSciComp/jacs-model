package org.janelia.model.access.domain.dao.searchables;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.janelia.model.access.cdi.AsyncIndex;
import org.janelia.model.access.domain.dao.ColorDepthImageDao;
import org.janelia.model.access.domain.search.DomainObjectIndexer;
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
    public long countColorDepthMIPs(String ownerKey, String libraryName, String alignmentSpace, List<String> matchingNames, List<String> matchingFilepaths) {
        return colorDepthImageDao.countColorDepthMIPs(ownerKey, libraryName, alignmentSpace, matchingNames, matchingFilepaths);
    }

    @Override
    public Stream<ColorDepthImage> streamColorDepthMIPs(String ownerKey, String libraryName, String alignmentSpace, List<String> matchingNames, List<String> matchingFilepaths, int offset, int length) {
        return colorDepthImageDao.streamColorDepthMIPs(ownerKey, libraryName, alignmentSpace, matchingNames, matchingFilepaths, offset, length);
    }

    @Override
    public void updatePublicUrls(ColorDepthImage cdmi) {
        colorDepthImageDao.updatePublicUrls(cdmi);
    }
}

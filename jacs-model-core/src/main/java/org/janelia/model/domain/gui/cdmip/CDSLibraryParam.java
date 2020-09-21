package org.janelia.model.domain.gui.cdmip;

import org.apache.commons.lang3.StringUtils;

public class CDSLibraryParam {

    private String libraryName;
    private String searchableImagesLocation;
    private String gradientImagesLocation;
    private String zgapMaskImagesLocation;
    private String viewableImagesLocation;

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public boolean hasSearchableImagesLocation() {
        return StringUtils.isNotBlank(searchableImagesLocation);
    }

    public String getSearchableImagesLocation() {
        return searchableImagesLocation;
    }

    public void setSearchableImagesLocation(String searchableImagesLocation) {
        this.searchableImagesLocation = searchableImagesLocation;
    }

    public boolean hasGradientImagesLocation() {
        return StringUtils.isNotBlank(gradientImagesLocation);
    }

    public String getGradientImagesLocation() {
        return gradientImagesLocation;
    }

    public void setGradientImagesLocation(String gradientImagesLocation) {
        this.gradientImagesLocation = gradientImagesLocation;
    }

    public boolean hasZgapMaskImagesLocation() {
        return StringUtils.isNotBlank(zgapMaskImagesLocation);
    }

    public String getZgapMaskImagesLocation() {
        return zgapMaskImagesLocation;
    }

    public void setZgapMaskImagesLocation(String zgapMaskImagesLocation) {
        this.zgapMaskImagesLocation = zgapMaskImagesLocation;
    }

    public String getViewableImagesLocation() {
        return viewableImagesLocation;
    }

    public void setViewableImagesLocation(String viewableImagesLocation) {
        this.viewableImagesLocation = viewableImagesLocation;
    }
}

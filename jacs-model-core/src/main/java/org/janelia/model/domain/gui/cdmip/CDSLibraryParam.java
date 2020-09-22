package org.janelia.model.domain.gui.cdmip;

import org.apache.commons.lang3.StringUtils;

public class CDSLibraryParam {

    private String libraryName;
    private String searchableVariant;
    private String gradientVariant;
    private String zgapMaskVariant;
    private String viewableVariant;

    public String getLibraryName() {
        return libraryName;
    }

    public void setLibraryName(String libraryName) {
        this.libraryName = libraryName;
    }

    public boolean hasSearchableVariant() {
        return StringUtils.isNotBlank(searchableVariant);
    }

    public String getSearchableVariant() {
        return searchableVariant;
    }

    public void setSearchableVariant(String searchableVariant) {
        this.searchableVariant = searchableVariant;
    }

    public boolean hasGradientVariant() {
        return StringUtils.isNotBlank(gradientVariant);
    }

    public String getGradientVariant() {
        return gradientVariant;
    }

    public void setGradientVariant(String gradientVariant) {
        this.gradientVariant = gradientVariant;
    }

    public boolean hasZgapMaskVariant() {
        return StringUtils.isNotBlank(zgapMaskVariant);
    }

    public String getZgapMaskVariant() {
        return zgapMaskVariant;
    }

    public void setZgapMaskVariant(String zgapMaskVariant) {
        this.zgapMaskVariant = zgapMaskVariant;
    }

    public String getViewableVariant() {
        return viewableVariant;
    }

    public void setViewableVariant(String viewableVariant) {
        this.viewableVariant = viewableVariant;
    }
}

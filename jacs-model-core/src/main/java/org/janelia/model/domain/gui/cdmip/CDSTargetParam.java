package org.janelia.model.domain.gui.cdmip;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CDSTargetParam {

    public static class Builder {
        private final CDSTargetParam cdsTargetParam;

        private Builder(String libraryName) {
            cdsTargetParam = new CDSTargetParam();
            cdsTargetParam.setLibraryName(libraryName);
        }

        public Builder withSearchableVariant(String searchableVariant) {
            cdsTargetParam.setSearchableVariant(searchableVariant);
            return this;
        }

        public Builder withGradientVariant(String gradientVariant) {
            cdsTargetParam.setGradientVariant(gradientVariant);
            return this;
        }

        public Builder withZGapMaskVariant(String zgapMaskVariant) {
            cdsTargetParam.setZgapMaskVariant(zgapMaskVariant);
            return this;
        }

        public CDSTargetParam build() {
            return cdsTargetParam;
        }
    }

    public static Builder builder(String libraryName) {
        return new Builder(libraryName);
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CDSTargetParam that = (CDSTargetParam) o;

        return new EqualsBuilder()
                .append(libraryName, that.libraryName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(libraryName)
                .toHashCode();
    }


}

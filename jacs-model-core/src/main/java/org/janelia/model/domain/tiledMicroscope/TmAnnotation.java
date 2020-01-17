package org.janelia.model.domain.tiledMicroscope;

import java.util.Date;

public interface TmAnnotation {
    public Long getId();
    public void setId(Long id);
    public Double getX();
    public void setX(Double x);
    public Double getY();
    public void setY(Double y);
    public Double getZ();
    public void setZ(Double z);
    public Date getCreationDate();
    public void setCreationDate(Date creationDate);
    public Date getModificationDate();
    public void setModificationDate(Date modificationDate);
    public void updateModificationDate();

}

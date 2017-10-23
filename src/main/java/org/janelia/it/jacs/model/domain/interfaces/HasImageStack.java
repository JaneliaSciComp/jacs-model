package org.janelia.it.jacs.model.domain.interfaces;

/**
 * Methods for exposing an underlying image stack.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface HasImageStack {

    public String getAnatomicalArea();

    public String getImageSize();

    public String getOpticalResolution();

    public String getChannelColors();

    public String getChannelSpec();

}
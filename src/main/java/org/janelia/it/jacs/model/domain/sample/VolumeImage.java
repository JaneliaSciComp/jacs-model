/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.janelia.it.jacs.model.domain.sample;

/**
 * This is a derivative class--not directly stored in the database, but rather
 * built out of things which were.
 *
 * @author fosterl
 */
public class VolumeImage implements Viewable2d, Viewable3d, Masked3d  {
    private String imagePath2d;
    private String imagePath3d;
    private String imagePathMask3d;
    private String imagePathChan3d;

    /**
     * Construct this bean-ish class with all its components.
     * 
     * @param imagePath2d a 2-dimensional, flat image
     * @param imagePath3d image with depth
     * @param imagePathMask3d special format used by AlignmentBoard...
     * @param imagePathChan3d ...other half of mask/chan.
     */
    public VolumeImage(String imagePath2d, String imagePath3d, String imagePathMask3d, String imagePathChan3d) {
        this.imagePath2d = imagePath2d;
        this.imagePath3d = imagePath3d;
        this.imagePathMask3d = imagePathMask3d;
        this.imagePathChan3d = imagePathChan3d;
    }

    @Override
    public String get2dImageFilepath() {
        return imagePath2d;
    }

    @Override
    public String get3dImageFilepath() {
        return imagePath3d;
    }

    @Override
    public String getMask3dImageFilepath() {
        return imagePathMask3d;
    }

    @Override
    public String getChan3dImageFilepath() {
        return imagePathChan3d;
    }
    
}

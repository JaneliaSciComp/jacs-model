package org.janelia.it.jacs.model.domain.tiledMicroscope;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.it.jacs.shared.utils.ColorUtils;

/**
 * Color model for a single channel.
 *
 * @see org.janelia.console.viewerapi.model.ChannelColorModel
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class TmChannelColorModel {

    private int blackLevel;
    private double gamma;
    private int whiteLevel;
    private String colorHex;
    private boolean visible;
    private double combiningConstant;

    public TmChannelColorModel() {
    }

    public static TmChannelColorModel copy(TmChannelColorModel channel) {
        TmChannelColorModel copy = new TmChannelColorModel();
        copy.setBlackLevel(channel.getBlackLevel());
        copy.setGamma(channel.getGamma());
        copy.setWhiteLevel(channel.getWhiteLevel());
        copy.setColorHex(channel.getColorHex());
        copy.setVisible(channel.isVisible());
        copy.setCombiningConstant(channel.getCombiningConstant());
        return copy;
    }
    
    public int getBlackLevel() {
        return blackLevel;
    }

    public void setBlackLevel(int blackLevel) {
        this.blackLevel = blackLevel;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public int getWhiteLevel() {
        return whiteLevel;
    }

    public void setWhiteLevel(int whiteLevel) {
        this.whiteLevel = whiteLevel;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    @JsonIgnore
    public Color getColor() {
        return ColorUtils.fromHex(colorHex);
    }

    @JsonIgnore
    public void setColor(Color color) {
        this.colorHex = ColorUtils.toHex(color);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public double getCombiningConstant() {
        return combiningConstant;
    }

    public void setCombiningConstant(double combiningConstant) {
        this.combiningConstant = combiningConstant;
    }
}

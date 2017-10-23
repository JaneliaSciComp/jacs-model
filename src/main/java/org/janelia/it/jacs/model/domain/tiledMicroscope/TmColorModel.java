package org.janelia.it.jacs.model.domain.tiledMicroscope;

import java.util.ArrayList;
import java.util.List;

/**
 * Color model for a viewer.
 *
 * @see org.janelia.console.viewerapi.model.ImageColorModel
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class TmColorModel {

    private Integer channelCount;
    private boolean blackSynchronized;
    private boolean gammaSynchronized;
    private boolean whiteSynchronized;
    private List<TmChannelColorModel> channels = new ArrayList<>();

    public TmColorModel() {
    }
    
    public static TmColorModel copy(TmColorModel colorModel) {
        TmColorModel copy = new TmColorModel();
        copy.setChannelCount(colorModel.getChannelCount());
        copy.setBlackSynchronized(colorModel.isBlackSynchronized());
        copy.setGammaSynchronized(colorModel.isGammaSynchronized());
        copy.setWhiteSynchronized(colorModel.isWhiteSynchronized());
        for(TmChannelColorModel channel : colorModel.getChannels()) {
            copy.getChannels().add(TmChannelColorModel.copy(channel));
        }
        return copy;
    }

    public Integer getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(Integer channelCount) {
        this.channelCount = channelCount;
    }

    public boolean isBlackSynchronized() {
        return blackSynchronized;
    }

    public void setBlackSynchronized(boolean blackSynchronized) {
        this.blackSynchronized = blackSynchronized;
    }

    public boolean isGammaSynchronized() {
        return gammaSynchronized;
    }

    public void setGammaSynchronized(boolean gammaSynchronized) {
        this.gammaSynchronized = gammaSynchronized;
    }

    public boolean isWhiteSynchronized() {
        return whiteSynchronized;
    }

    public void setWhiteSynchronized(boolean whiteSynchronized) {
        this.whiteSynchronized = whiteSynchronized;
    }

    public List<TmChannelColorModel> getChannels() {
        return channels;
    }

    public void setChannels(List<TmChannelColorModel> channels) {
        this.channels = channels;
    }
}

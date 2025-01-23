package org.janelia.model.domain.tiledMicroscope;

import org.janelia.model.domain.tiledMicroscope.TmSample;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

/**
 * This stores a global ID for locating a HC stack, loading the appropriate Sample/Workspace in the stack, and
 * navigating to a specific viewpoint in that Sample/Workspace.
 */
public class DeepLink {
    public String getHortaCloudStack() {
        return hortaCloudStack;
    }

    public void setHortaCloudStack(String hortaCloudStack) {
        this.hortaCloudStack = hortaCloudStack;
    }

    public TmWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(TmWorkspace workspace) {
        this.workspace = workspace;
    }

    public TmSample getSample() {
        return sample;
    }

    public void setSample(TmSample sample) {
        this.sample = sample;
    }

    public double getViewFocusX() {
        return viewFocusX;
    }

    public void setViewFocusX(double viewFocusX) {
        this.viewFocusX = viewFocusX;
    }

    public double getViewFocusY() {
        return viewFocusY;
    }

    public void setViewFocusY(double viewFocusY) {
        this.viewFocusY = viewFocusY;
    }

    public double getViewFocusZ() {
        return viewFocusZ;
    }

    public void setViewFocusZ(double viewFocusZ) {
        this.viewFocusZ = viewFocusZ;
    }

    public double getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(double zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public float[] getCameraRotation() {
        return cameraRotation;
    }

    public void setCameraRotation(float[] cameraRotation) {
        this.cameraRotation = cameraRotation;
    }

    String hortaCloudStack;
    TmWorkspace workspace;
    TmSample sample;
    double viewFocusX;
    double viewFocusY;
    double viewFocusZ;
    double zoomLevel;
    private float[] cameraRotation;
}

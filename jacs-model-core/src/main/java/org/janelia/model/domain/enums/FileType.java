package org.janelia.model.domain.enums;

/**
 * Different types of files which may be associated with an object that implements the HasFiles interface.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public enum FileType {

    // Virtual file types for driving the image browser
    FirstAvailable2d("First Available", true, true, false, true),
    FirstAvailable3d("First Available Stack", false, true, false, true),

    // Virtual file types for driving the Download Wizard
    ColorDepthMips("Color Depth Projections", true, false, false, true),

    // Stacks
    LosslessStack("Lossless Stack (e.g. LSM/RAW/PBD)", false, true, false),
    VisuallyLosslessStack("Visually Lossless Stack (e.g. H5J)", false, true, false),
    FastStack("Fast-loading Stack (e.g. MP4)", false, true, false),

    // Metadata files
    LsmMetadata("LSM Metadata", false, false, true),
    
    // Maximum Intensity Projections (MIPs) 
    SignalMip("Signal MIP", true, false, false),
    Signal1Mip("Signal MIP ch1", true, false, false),
    Signal2Mip("Signal MIP ch2", true, false, false),
    Signal3Mip("Signal MIP ch3", true, false, false),
    Signal4Mip("Signal MIP ch4", true, false, false),
    ReferenceMip("Reference MIP", true, false, false),
    AllMip("All Channel MIP", true, false, false),
    RefSignal1Mip("Reference+Signal MIP ch1", true, false, false),
    RefSignal2Mip("Reference+Signal MIP ch2", true, false, false),
    RefSignal3Mip("Reference+Signal MIP ch3", true, false, false),

    // Color Depth Projections
    ColorDepthMip1("Color Depth Projection ch1", true, false, false),
    ColorDepthMip2("Color Depth Projection ch2", true, false, false),
    ColorDepthMip3("Color Depth Projection ch3", true, false, false),
    ColorDepthMip4("Color Depth Projection ch4", true, false, false),

    // Movies
    SignalMovie("Signal Movie", false, false, false),
    ReferenceMovie("Reference Movie", false, false, false),
    AllMovie("Reference+Signal Movie", false, false, false),
    
    // Alignment outputs
    AlignmentVerificationMovie("Alignment Verification Movie", false, false, false),
    AlignedCondolidatedLabel("Aligned Consolidated Label", false, false, false),
    
    // Heatmaps for pattern data
    HeatmapStack("Heatmap Stack", false, true, false),
    HeatmapMip("Heatmap MIP", true, false, false),
    
    // Neuron separation
    NeuronSeparatorResult("Neuron Separator Result", false, true, false),
    NeuronAnnotatorLabel("Neuron Annotator Label (PBD)", false, true, false),
    NeuronAnnotatorSignal("Neuron Annotator Signal (PBD)", false, true, false),
    NeuronAnnotatorReference("Neuron Annotator Reference (PBD)", false, true, false),
    MaskFile("Mask File", false, false, false),
    ChanFile("Chan File", false, false, false),
    
    // Cell Counting Results
    CellCountPlan("Cell Counting Plan", false, false, true),
    CellCountReport("Cell Counting Report", false, false, true),
    CellCountStack("Cell Counting Stack", false, true, false),
    CellCountStackMip("Cell Counting Stack MIP", true, false, false),
    CellCountImage("Cell Counting Image", false, true, false),
    CellCountImageMip("Cell Counting Image MIP", true, false, false),

    // Tiled microscope data for MouseLight
    LargeVolumeOctree("Large Volume Octree", false, false, false),
    LargeVolumeKTX("Large Volume KTX", false, false, false),
    TwoPhotonAcquisition("Two Photon Acquisition", false, false, false),
    CompressedAcquisition("Compressed Acquisition", false, false, false),

    // Legacy files
    Unclassified2d("2D Image", true, false, false),
    Unclassified3d("3D Image", false, true, false),
    UnclassifiedAscii("Text File", false, false, true);

    private final String label;
    private final boolean is2dImage;
    private final boolean is3dImage;
    private final boolean isAscii;
    private final boolean isVirtual;

    FileType(String label, boolean is2dImage, boolean is3dImage, boolean isAscii) {
        this(label, is2dImage, is3dImage, isAscii, false);
    }

    FileType(String label, boolean is2dImage, boolean is3dImage, boolean isAscii, boolean isVirtual) {
        this.label = label;
        this.is2dImage = is2dImage;
        this.is3dImage = is3dImage;
        this.isAscii = isAscii;
        this.isVirtual = isVirtual;
    }
    
    public String getLabel() {
        return label;
    }

    public static FileType getByLabel(String label) {
        FileType[] values = FileType.values();
        for (FileType value : values) {
            if (value.getLabel().equals(label)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Does the file type represent a 2D image that can be displayed on screen?
     * @return true if the file is a 2d image
     */
    public boolean is2dImage() {
        return is2dImage;
    }

    /**
     * Does the file type represent a 3D image or image stack?
     * @return true if the file is a 3d image
     */
    public boolean is3dImage() {
        return is3dImage;
    }

    /**
     * Does the file type represent an ASCII file that can be displayed in a text editor?
     * @return true if the file is ASCII
     */
    public boolean isAscii() {
        return isAscii;
    }

    /**
     * Virtual file types are not used in the database. They are only used in the UI for user selections.
     * @return true if the file type is virtual
     */
    public boolean isVirtual() {
        return isVirtual;
    }
}

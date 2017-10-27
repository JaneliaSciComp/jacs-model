package org.janelia.model.domain.enums;

public enum PipelineProcess implements NamedEnum {

    // The enum must match the process file name after PipelineConfig_
    CellCounting("Cell Counting Pipeline"),

    DicksonVTGal4Screen("Dickson VT Gal4 Screen Pipeline"),

    DitpAlignment("DITP Alignment Pipeline"),
    DitpMCFOCase1("DITP MCFO Case 1 Pipeline"),
    DitpPolarityCase3("DITP Polarity Case 3 Pipeline"),

    FlyLightCentralBrain("FlyLight Central Brain Pipeline"),
    FlyLightMCFO40x("FlyLight MCFO 40X Pipeline"),
    FlyLightOpticLobe("FlyLight Optic Lobe Pipeline"),
    FlyLightProjectionWithChannelSpec("FlyLight Projection With Channel Spec Pipeline"),
    FlyLightPairedSample("FlyLight Paired Sample"),
    FlyLightPairedSamplePolarity("FlyLight Paired Sample Polarity"),
    FlyLightPairedSampleMCFO("FlyLight Paired Sample MCFO"),
    FlyLightUnaligned("FlyLight Unaligned Pipeline"),
    FlyLightWholeBrain("FlyLight Whole Brain Pipeline"),
    FlyLightWholeBrain64x("FlyLight Whole Brain 63x Pipeline"),
    FlyLightUnalignedNoSeparation("FlyLight Unaligned/No Separation Pipeline"),
    FlyLightScreen("Fly Light Gen1 Screen"),

    LeetCentralBrain63x("Lee Central Brain 63x Pipeline"),
    LeetWholeBrain40x("Lee Whole Brain 40x Pipeline"),
    LeetWholeBrain40x512pxINT("Lee Whole Brain 40x 512px INTensity Pipeline"),
    LeetWholeBrain40xImproved("Lee Whole Brain 40x Improved Pipeline"),
    LeetUnaligned("Lee Unaligned Pipeline"),
    LeetUnalignedNoSeparation("Lee Unaligned/No Separation Pipeline"),

    NernaLeftOpticLobe("Aljoscha Left Optic Lobe 63x Pipeline"),
    NernaMCFOCase1("Aljoscha MCFO Case 1 Pipeline"),
    NernaMCFOCase1Without20xMerge("Aljoscha MCFO Case 1 Without 20x Merge Pipeline"),
    NernaRightOpticLobe("Aljoscha Right Optic Lobe 63x Pipeline"),
    NernaPolarityCase3("Aljoscha Polarity Case 3 Pipeline"),

    PolarityCase3("Polarity Case 3 Pipeline"),
    
    PTR10ProjectionOnly("PTR 10 Projection Only Pipeline"),
    PTR20JBAAlignment("PTR 20 JBA Alignment Pipeline"),
    PTR30ANTSAlignment("PTR 30 ANTS Alignment Pipeline"),
    PTR40NeuronSeparation("PTR 40 Neuron Separation Pipeline"),

    SimpsonDescending("Simpson Lab Sensory Descending Alignment Pipeline"),
    
    TerraIncognita40x("Terra Incognita 40x Whole Brain Pipeline"),
    
    WolfftMCFOCase1("Tanya Central Brain MCFO Case 1 Pipeline"),
    WolfftMCFOCase1Unaligned("Tanya MCFO Case 1 Unaligned Pipeline"),

    YoshiMB63xFlpout1024pxINT("Yoshi MB Flp-out 63x 1024px INTensity Pipeline"),
    YoshiMB63xFlpout512pxINT("Yoshi MB Flp-out 63x 512px INTensity Pipeline"),
    YoshiMB63xLexAGal41024pxINT("Yoshi MB LexA-GAL4 63x 1024px INTensity Pipeline"),
    YoshiMB63xLexAGal4512pxINT("Yoshi MB LexA-GAL4 63x 512px INTensity Pipeline"),
    YoshiMBPolarityCase1("Yoshi MB Polarity Case 1 Pipeline"),
    YoshiMBPolarityCase2("Yoshi MB Polarity Case 2 Pipeline"),
    YoshiMBPolarityCase3("Yoshi MB Polarity Case 3 Pipeline"),
    YoshiMBPolarityCase4("Yoshi MB Polarity Case 4 Pipeline"),
    YoshiMBSplitMCFOCase1("Yoshi MB Split MCFO Case 1 Pipeline"),
    YoshiMacroMB63xFlpout("Yoshi MB 63x Flpout Fiji Macro Pipeline"),
    YoshiMacroPolarityCase1("Yoshi Polarity Case 1 Macro Pipeline"),
    YoshiMacroPolarityCase2("Yoshi Polarity Case 2 Macro Pipeline"),
    YoshiMacroPolarityCase3("Yoshi Polarity Case 3 Macro Pipeline"),
    YoshiMacroPolarityCase4("Yoshi Polarity Case 4 Macro Pipeline"),
    YoshiMacroMCFOCase1("Yoshi MCFO Case 1 Macro Pipeline");

    private String name;

    private PipelineProcess(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

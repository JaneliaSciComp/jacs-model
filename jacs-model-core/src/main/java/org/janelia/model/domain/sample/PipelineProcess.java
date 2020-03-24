package org.janelia.model.domain.sample;

import org.janelia.model.domain.enums.NamedEnum;

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
    FlyLightProjectionWithoutCorrection("FlyLight Projection Without Distortion Correction Pipeline"),
    FlyLightPairedSample("FlyLight Paired Sample"),
    FlyLightPairedSamplePolarity("FlyLight Paired Sample Polarity"),
    FlyLightPairedSampleMCFO("FlyLight Paired Sample MCFO"),
    FlyLightUnaligned("FlyLight Unaligned Pipeline"),
    FlyLightWholeBrain("FlyLight Whole Brain Pipeline"),
    FlyLightWholeBrain64x("FlyLight Whole Brain 63x Pipeline"),
    FlyLightUnalignedNoSeparation("FlyLight Unaligned/No Separation Pipeline"),
    FlyLightScreen("FlyLight Gen1 Screen"),
    FlyLightFISH("FlyLight FISH"),
    FlyLightPairedSample2018("FlyLight Paired Sample 2018 Alignment Pipeline"),
    FlyLightPairedSample2018MCFO("FlyLight Paired MCFO 2018 Alignment Pipeline"),
    FlyLightPairedSample2018Polarity("FlyLight Paired Polarity 2018 Alignment Pipeline"),
    FlyLightPairedSample2018MCFOFull("FlyLight Paired MCFO 2018 (Full Scale 63x) Alignment Pipeline"),
    FlyLightPairedSample2018PolarityFull("FlyLight Paired Polarity 2018 (Full Scale 63x) Alignment Pipeline"),
    FlyLightMCFO40x2018("FlyLight MCFO 40x 2018 Alignment Pipeline"),
    
    FlyLightPolarityCase3("Fly Light Polarity Case 3 Pipeline"),
    FlyLightPolarityCase5("Fly Light Polarity Case 5 Pipeline"),
    
    LeetCentralBrain63x("Lee Central Brain 63x Pipeline"),
    LeetWholeBrain40x("Lee Whole Brain 40x Pipeline"),
    LeetWholeBrain40x512pxINT("Lee Whole Brain 40x 512px INTensity Pipeline"),
    LeetWholeBrain40xImproved("Lee Whole Brain 40x Improved Pipeline"),
    LeetUnaligned("Lee Unaligned Pipeline"),
    LeetUnalignedNoSeparation("Lee Unaligned/No Separation Pipeline"),

    NernaLeftOpticLobe("Aljoscha Left Optic Lobe 63x Pipeline"),
    NernaMCFOCase1("Aljoscha MCFO Case 1 Pipeline"),
    NernaMCFOCase1WithoutGrouper("Aljoscha MCFO Case 1 (Without Grouper) Pipeline"),
    NernaMCFOCase1WithGrouper("Aljoscha MCFO Case 1 (With Grouper) Pipeline"),
    NernaMCFOCase1Without20xMerge("Aljoscha MCFO Case 1 (Without 20x Merge) Pipeline"),
    NernaRightOpticLobe("Aljoscha Right Optic Lobe 63x Pipeline"),
    NernaPolarityCase3("Aljoscha Polarity Case 3 Pipeline"),
    NernaPolarityCase3WithoutGrouper("Aljoscha Polarity Case 3 (Without Grouper) Pipeline"),
    NernaPolarityCase3WithGrouper("Aljoscha Polarity Case 3 (With Grouper) Pipeline"),
    
    PTR10ProjectionOnly("PTR 10 Projection Only Pipeline"),
    PTR20JBAAlignment("PTR 20 JBA Alignment Pipeline"),
    PTR30ANTSAlignment("PTR 30 ANTS Alignment Pipeline"),
    PTR30CMTKAlignment20x63x("PTR 30 CMTK Alignment (20x/63x) Pipeline"),
    PTR30CMTKAlignment40x("PTR 30 CMTK Alignment (40x) Pipeline"),
    PTR30Tango20x63x("PTR 30 Tango (20x/63x) Pipeline"),
    PTR30Tango40x("PTR 30 Tango (40x) Pipeline"),
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
    YoshiMacroMCFOCase1("Yoshi MCFO Case 1 Macro Pipeline"),

    TrumanLarval40x("Truman Larval 40x Pipeline");
    
    private String name;

    private PipelineProcess(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

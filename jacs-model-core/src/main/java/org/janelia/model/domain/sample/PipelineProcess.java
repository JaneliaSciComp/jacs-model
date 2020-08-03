package org.janelia.model.domain.sample;

import org.janelia.model.domain.enums.NamedEnum;

public enum PipelineProcess implements NamedEnum {

    // The enum must match the process file name after PipelineConfig_

    FlyLightSample("FlyLight Sample 20x/40x/63x Alignment Pipeline"),
    FlyLightSampleMCFO("FlyLight Sample 20x/40x/63x MCFO Alignment Pipeline"),
    FlyLightSamplePolarity("FlyLight Sample 20x/40x/63x Polarity Alignment Pipeline"),
    FlyLightFISH("FlyLight FISH Pipeline"),
    FlyLightUnaligned("FlyLight Unaligned Pipeline"),
    FlyLightProjectionWithoutCorrection("FlyLight Unaligned (No Distortion Correction) Pipeline"),
    FlyLightProjectionWithChannelSpec("FlyLight Projection With Channel Spec Pipeline"),

    FlyLightMCFO40x2018("FlyLight MCFO 40x 2018 Alignment Pipeline"),
    FlyLightPairedSample2018("FlyLight Paired Sample 2018 Alignment Pipeline"),
    FlyLightPairedSample2018MCFO("FlyLight Paired MCFO 2018 Alignment Pipeline"),
    FlyLightPairedSample2018Polarity("FlyLight Paired Polarity 2018 Alignment Pipeline"),
    FlyLightPairedSample2018MCFOFull("FlyLight Paired MCFO 2018 (Full Scale 63x) Alignment Pipeline"),
    FlyLightPairedSample2018PolarityFull("FlyLight Paired Polarity 2018 (Full Scale 63x) Alignment Pipeline"),
    FlyLightPolarityCase3("FlyLight Polarity Case 3 Pipeline"),
    FlyLightPolarityCase5("FlyLight Polarity Case 5 Pipeline"),

    FlyLightCentralBrain("Legacy FlyLight Central Brain Pipeline"),
    FlyLightOpticLobe("Legacy FlyLight Optic Lobe Pipeline"),
    FlyLightWholeBrain("Legacy FlyLight Whole Brain Pipeline"),
    FlyLightWholeBrain64x("Legacy FlyLight Whole Brain 63x Pipeline"),
    FlyLightPairedSample("Legacy FlyLight Paired Sample"),
    FlyLightPairedSamplePolarity("Legacy FlyLight Paired Sample Polarity"),
    FlyLightPairedSampleMCFO("Legacy FlyLight Paired Sample MCFO"),
    FlyLightMCFO40x("Legacy FlyLight MCFO 40X Pipeline"),

    PTR10ProjectionOnly("PTR 10 Projection Only Pipeline"),
    PTR30CMTKAlignment20x63x("PTR 30 CMTK Alignment (20x/63x) Pipeline"),
    PTR30CMTKAlignment40x("PTR 30 CMTK Alignment (40x) Pipeline"),
    PTR30Tango20x63x("PTR 30 Tango (20x/63x) Pipeline"),
    PTR30Tango40x("PTR 30 Tango (40x) Pipeline"),

    NernaLeftOpticLobe("Nern Left Optic Lobe 63x Pipeline"),
    NernaRightOpticLobe("Nern Right Optic Lobe 63x Pipeline"),
    NernaMCFOCase1("Nern MCFO Case 1 Pipeline"),
    NernaMCFOCase1WithoutGrouper("Nern MCFO Case 1 (Without Grouper) Pipeline"),
    NernaMCFOCase1WithGrouper("Nern MCFO Case 1 (With Grouper) Pipeline"),
    NernaMCFOCase1Without20xMerge("Nern MCFO Case 1 (Without 20x Merge) Pipeline"),
    NernaPolarityCase3("Nern Polarity Case 3 Pipeline"),
    NernaPolarityCase3WithoutGrouper("Nern Polarity Case 3 (Without Grouper) Pipeline"),
    NernaPolarityCase3WithGrouper("Nern Polarity Case 3 (With Grouper) Pipeline"),

    WolfftMCFOCase1("Wolff Central Brain MCFO Case 1 Pipeline"),
    WolfftMCFOCase1Unaligned("Wolff MCFO Case 1 Unaligned Pipeline"),

    YoshiMB63xFlpout1024pxINT("Aso MB Flp-out 63x 1024px INTensity Pipeline"),
    YoshiMB63xFlpout512pxINT("Aso MB Flp-out 63x 512px INTensity Pipeline"),
    YoshiMB63xLexAGal41024pxINT("Aso MB LexA-GAL4 63x 1024px INTensity Pipeline"),
    YoshiMB63xLexAGal4512pxINT("Aso MB LexA-GAL4 63x 512px INTensity Pipeline"),
    YoshiMBPolarityCase1("Aso MB Polarity Case 1 Pipeline"),
    YoshiMBPolarityCase2("Aso MB Polarity Case 2 Pipeline"),
    YoshiMBPolarityCase3("Aso MB Polarity Case 3 Pipeline"),
    YoshiMBPolarityCase4("Aso MB Polarity Case 4 Pipeline"),
    YoshiMBSplitMCFOCase1("Aso MB Split MCFO Case 1 Pipeline"),

    LeetCentralBrain63x("Lee Central Brain 63x Pipeline"),
    LeetWholeBrain40x("Lee Whole Brain 40x Pipeline"),
    LeetWholeBrain40x512pxINT("Lee Whole Brain 40x 512px INTensity Pipeline"),
    LeetWholeBrain40xImproved("Lee Whole Brain 40x Improved Pipeline"),
    LeetUnaligned("Lee Unaligned Pipeline"),
    LeetUnalignedNoSeparation("Lee Unaligned/No Separation Pipeline"),

    TrumanLarval40x("Truman Larval 40x Pipeline");

    private String name;

    private PipelineProcess(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

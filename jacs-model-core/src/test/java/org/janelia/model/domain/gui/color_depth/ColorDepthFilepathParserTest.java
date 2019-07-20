package org.janelia.model.domain.gui.color_depth;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the ColorDepthFilepathParser.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthFilepathParserTest {

    @Test
    public void testExample1() throws Exception {

        String filepath = "/nrs/jacs/jacsData/filestore/system/ColorDepthMIPs/JFRC2013_63x/flylight_gen1_mcfo_case_1/GMR_SS00313-20170324_26_C5-40x-Brain-JFRC2013_63x-2391787101235445858-CH1_CDM.png";

        ColorDepthFilepathParser parser = ColorDepthFilepathParser.parse(filepath);
        Assert.assertEquals(filepath, parser.getFile().getAbsolutePath());
        Assert.assertEquals("Sample#2391787101235445858", parser.getSampleRef().toString());
        Assert.assertEquals("JFRC2013_63x", parser.getAlignmentSpace());
        Assert.assertEquals("flylight_gen1_mcfo_case_1", parser.getLibraryIdentifier());
        Assert.assertEquals("GMR_SS00313-20170324_26_C5", parser.getSampleName());
        Assert.assertEquals("40x", parser.getObjective());
        Assert.assertEquals("Brain", parser.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), parser.getChannelNumber());
    }
}

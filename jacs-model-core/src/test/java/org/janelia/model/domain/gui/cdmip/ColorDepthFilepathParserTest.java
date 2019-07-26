package org.janelia.model.domain.gui.cdmip;

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
        Assert.assertEquals("GMR_SS00313-20170324_26_C5", parser.getSampleName());
        Assert.assertEquals("40x", parser.getObjective());
        Assert.assertEquals("Brain", parser.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), parser.getChannelNumber());
    }

    @Test
    public void testExample2() throws Exception {

        String filepath = "/groups/jacs/jacsDev/devstore/system/ColorDepthMIPs/JRC2018_VNC_Unisex/flylight_splitgal4_drivers/GMR_SS00810-20140421_31_D1-20x-VNC-JRC2018_VNC_Unisex-2002548901436981346-CH1_CDM.png";

        ColorDepthFilepathParser parser = ColorDepthFilepathParser.parse(filepath);
        Assert.assertEquals(filepath, parser.getFile().getAbsolutePath());
        Assert.assertEquals("Sample#2002548901436981346", parser.getSampleRef().toString());
        Assert.assertEquals("JRC2018_VNC_Unisex", parser.getAlignmentSpace());
        Assert.assertEquals("GMR_SS00810-20140421_31_D1", parser.getSampleName());
        Assert.assertEquals("20x", parser.getObjective());
        Assert.assertEquals("VNC", parser.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), parser.getChannelNumber());
    }

    @Test
    public void testNestedExample() throws Exception {

        String filepath = "/groups/jacs/jacsDev/devstore/system/ColorDepthMIPs/JRC2018_VNC_Unisex/flylight_splitgal4_drivers/extra/nesting/GMR_SS00810-20140421_31_D1-20x-VNC-JRC2018_VNC_Unisex-2002548901436981346-CH1_CDM.png";

        ColorDepthFilepathParser parser = ColorDepthFilepathParser.parse(filepath);
        Assert.assertEquals(filepath, parser.getFile().getAbsolutePath());
        Assert.assertEquals("Sample#2002548901436981346", parser.getSampleRef().toString());
        Assert.assertEquals("JRC2018_VNC_Unisex", parser.getAlignmentSpace());
        Assert.assertEquals("GMR_SS00810-20140421_31_D1", parser.getSampleName());
        Assert.assertEquals("20x", parser.getObjective());
        Assert.assertEquals("VNC", parser.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), parser.getChannelNumber());
    }
}

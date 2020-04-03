package org.janelia.model.domain.gui.cdmip;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Tests for the ColorDepthFileComponents.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ColorDepthFileComponentsTest {

    @Test
    public void testExample1() {

        String filepath = "/nrs/jacs/jacsData/filestore/system/ColorDepthMIPs/JFRC2013_63x/flylight_gen1_mcfo_case_1/GMR_SS00313-20170324_26_C5-40x-Brain-JFRC2013_63x-2391787101235445858-CH1_CDM.png";

        ColorDepthFileComponents cdf = ColorDepthFileComponents.fromFilepath(filepath);
        Assert.assertEquals(new File(filepath).getAbsolutePath(), cdf.getFile().getAbsolutePath());
        Assert.assertEquals("Sample#2391787101235445858", cdf.getSampleRef().toString());
        Assert.assertEquals("JFRC2013_63x", cdf.getAlignmentSpace());
        Assert.assertEquals("GMR_SS00313-20170324_26_C5", cdf.getSampleName());
        Assert.assertEquals("40x", cdf.getObjective());
        Assert.assertEquals("Brain", cdf.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), cdf.getChannelNumber());
    }

    @Test
    public void testExample2() {

        String filepath = "/groups/jacs/jacsDev/devstore/system/ColorDepthMIPs/JRC2018_VNC_Unisex/flylight_splitgal4_drivers/GMR_SS00810-20140421_31_D1-20x-VNC-JRC2018_VNC_Unisex-2002548901436981346-CH1_CDM.png";

        ColorDepthFileComponents cdf = ColorDepthFileComponents.fromFilepath(filepath);
        Assert.assertEquals(new File(filepath).getAbsolutePath(), cdf.getFile().getAbsolutePath());
        Assert.assertEquals("Sample#2002548901436981346", cdf.getSampleRef().toString());
        Assert.assertEquals("JRC2018_VNC_Unisex", cdf.getAlignmentSpace());
        Assert.assertEquals("GMR_SS00810-20140421_31_D1", cdf.getSampleName());
        Assert.assertEquals("20x", cdf.getObjective());
        Assert.assertEquals("VNC", cdf.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), cdf.getChannelNumber());
    }

    @Test
    public void testNestedExample() {

        String filepath = "/groups/jacs/jacsDev/devstore/system/ColorDepthMIPs/JRC2018_VNC_Unisex/flylight_splitgal4_drivers/extra/nesting/GMR_SS00810-20140421_31_D1-20x-VNC-JRC2018_VNC_Unisex-2002548901436981346-CH1_CDM.png";

        ColorDepthFileComponents cdf = ColorDepthFileComponents.fromFilepath(filepath);
        Assert.assertEquals(new File(filepath).getAbsolutePath(), cdf.getFile().getAbsolutePath());
        Assert.assertEquals("Sample#2002548901436981346", cdf.getSampleRef().toString());
        Assert.assertEquals("JRC2018_VNC_Unisex", cdf.getAlignmentSpace());
        Assert.assertEquals("GMR_SS00810-20140421_31_D1", cdf.getSampleName());
        Assert.assertEquals("20x", cdf.getObjective());
        Assert.assertEquals("VNC", cdf.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), cdf.getChannelNumber());
    }

    @Test
    public void testAnMCFOExample() {

        String filepath = "/nrs/jacs/jacsData/filestore/system/ColorDepthMIPs/JRC2018_Unisex_20x_HR/flylight_gen1_mcfo_case_1/JRC_SS54549-20180523_31_A2-20x-Brain-JRC2018_Unisex_20x_HR-2542063477862695010-CH1_CDM.png";

        ColorDepthFileComponents cdf = ColorDepthFileComponents.fromFilepath(filepath);
        Assert.assertEquals(new File(filepath).getAbsolutePath(), cdf.getFile().getAbsolutePath());
        Assert.assertEquals("Sample#2542063477862695010", cdf.getSampleRef().toString());
        Assert.assertEquals("JRC2018_Unisex_20x_HR", cdf.getAlignmentSpace());
        Assert.assertEquals("JRC_SS54549-20180523_31_A2", cdf.getSampleName());
        Assert.assertEquals("20x", cdf.getObjective());
        Assert.assertEquals("Brain", cdf.getAnatomicalArea());
        Assert.assertEquals(new Integer(1), cdf.getChannelNumber());
    }

}

package org.janelia.model.domain;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Sample domain model. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleUtilsTests {

    @Test
    public void testLineName() {
        Assert.assertEquals("BJD_100A01",SampleUtils.getFragFromLineName("BJD_100A01_AE_01"));
    }

    @Test
    public void testFragName() {
        Assert.assertEquals("BJD_100A01",SampleUtils.getFragFromLineName("BJD_100A01"));
    }

    @Test
    public void testPlateWell() {
        Assert.assertEquals("100A01",SampleUtils.getPlateWellFromLineName("BJD_100A01_AE_01"));
    }

    @Test
    public void testPlateWell2() {
        Assert.assertEquals("100A01",SampleUtils.getPlateWellFromLineName("BJD_100A01"));
    }
}

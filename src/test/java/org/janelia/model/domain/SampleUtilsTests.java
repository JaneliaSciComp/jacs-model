package org.janelia.model.domain;

import org.janelia.model.access.domain.SampleUtils;
import org.janelia.model.domain.sample.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Tests for the Sample domain model. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SampleUtilsTests {

    @Test
    public void testLineName() throws Exception {
        Assert.assertEquals("BJD_100A01",SampleUtils.getFragFromLineName("BJD_100A01_AE_01"));
    }

    @Test
    public void testFragName() throws Exception {
        Assert.assertEquals("BJD_100A01",SampleUtils.getFragFromLineName("BJD_100A01"));
    }

    @Test
    public void testPlateWell() throws Exception {
        Assert.assertEquals("100A01",SampleUtils.getPlateWellFromLineName("BJD_100A01_AE_01"));
    }

    @Test
    public void testPlateWell2() throws Exception {
        Assert.assertEquals("100A01",SampleUtils.getPlateWellFromLineName("BJD_100A01"));
    }
}

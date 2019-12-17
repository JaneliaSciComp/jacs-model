package org.janelia.org.janelia.model.util;

import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.util.ReflectionHelper;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the ReflectionHelper class.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ReflectionHelperTest {

    @Test
    public void testGetString() throws Exception {
        DataSet ds = new DataSet();
        ds.setName("Test");
        Object name = ReflectionHelper.getUsingGetter(ds, "name");
        Assert.assertTrue(name instanceof String);
        Assert.assertEquals("Test", name);
    }

    @Test
    public void testGetBoolean() throws Exception {
        DataSet ds = new DataSet();
        ds.setSageSync(true);
        Object sageSync = ReflectionHelper.getUsingGetter(ds, "sageSync");
        Assert.assertTrue(sageSync instanceof Boolean);
        Assert.assertTrue((Boolean)sageSync);
    }

    @Test
    public void testSetString() throws Exception {
        DataSet ds = new DataSet();
        ReflectionHelper.setUsingSetter(ds, "name", "Test");
        Assert.assertEquals("Test", ds.getName());
    }

    // TODO: this doesn't work currently because the method has a primitive boolean parameter
//    @Test
//    public void testSetBoolean() throws Exception {
//        DataSet ds = new DataSet();
//        ReflectionHelper.setUsingSetter(ds, "sageSync", true);
//        Assert.assertTrue(ds.isSageSync());
//    }

    @Test
    public void testNoSuchMethod() throws Exception {
        DataSet ds = new DataSet();
        ds.setSageSync(true);
        boolean caughtException = false;
        try {
            ReflectionHelper.getUsingGetter(ds, "testNotExistingField");
        }
        catch (NoSuchMethodException e) {
            caughtException = true;
        }
        Assert.assertTrue(caughtException);
    }
}

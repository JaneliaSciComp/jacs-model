package org.janelia.model.domain;

import com.google.common.collect.Sets;
import org.janelia.model.domain.compartments.CompartmentSet;
import org.janelia.model.domain.files.N5Container;
import org.janelia.model.domain.gui.alignment_board.AlignmentBoard;
import org.janelia.model.domain.gui.alignment_board.AlignmentContext;
import org.janelia.model.domain.gui.cdmip.ColorDepthMask;
import org.janelia.model.domain.gui.search.Filter;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.domain.sample.CuratedNeuron;
import org.janelia.model.domain.sample.DataSet;
import org.janelia.model.domain.sample.Image;
import org.janelia.model.domain.sample.LSMImage;
import org.janelia.model.domain.sample.LineRelease;
import org.janelia.model.domain.sample.NeuronFragment;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.screen.FlyLine;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.domain.workspace.Workspace;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the base classes of the domain object model.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainObjectTests {

    @Test
    public void testGetType() {
        Assert.assertEquals("Alignment Board",(new AlignmentBoard()).getType());
        Assert.assertEquals("Alignment Context", (new AlignmentContext()).getType());
        Assert.assertEquals("Annotation",(new Annotation()).getType());
        Assert.assertEquals("Compartment Set",(new CompartmentSet()).getType());
        Assert.assertEquals("Data Set",(new DataSet()).getType());
        Assert.assertEquals("Search",(new Filter()).getType());
        Assert.assertEquals("Fly Line",(new FlyLine()).getType());
        Assert.assertEquals("Image",(new Image()).getType());
        Assert.assertEquals("LSM Image",(new LSMImage()).getType());
        Assert.assertEquals("Line Release",(new LineRelease()).getType());
        Assert.assertEquals("Neuron Fragment",(new NeuronFragment()).getType());
        Assert.assertEquals("Curated Neuron",(new CuratedNeuron()).getType());
        Assert.assertEquals("Sample",(new Sample()).getType());
        Assert.assertEquals("Folder",(new TreeNode()).getType());
        Assert.assertEquals("Workspace",(new Workspace()).getType());
        Assert.assertEquals("Ontology",(new Ontology()).getType());
    }
    
    @Test
    public void testGetSearchType() {
        Assert.assertEquals(null,(new AlignmentBoard()).getSearchType());
        Assert.assertEquals(null,(new Annotation()).getSearchType());
        Assert.assertEquals(null,(new CompartmentSet()).getSearchType());
        Assert.assertEquals("dataSet",(new DataSet()).getSearchType());
        Assert.assertEquals("filter",(new Filter()).getSearchType());
        Assert.assertEquals("image",(new Image()).getSearchType());
        Assert.assertEquals("lsmImage",(new LSMImage()).getSearchType());
        Assert.assertEquals("sample",(new Sample()).getSearchType());
        Assert.assertEquals("treeNode",(new TreeNode()).getSearchType());
        Assert.assertEquals("treeNode",(new Workspace()).getSearchType());
    }

    @Test
    public void testGetSearchTypes() {
        Assert.assertEquals(Sets.newHashSet("dataSet"),(new DataSet()).getSearchTypes());
        Assert.assertEquals(Sets.newHashSet("filter"),(new Filter()).getSearchTypes());
        Assert.assertEquals(Sets.newHashSet("image"),(new Image()).getSearchTypes());
        Assert.assertEquals(Sets.newHashSet("lsmImage","image"),(new LSMImage()).getSearchTypes());
        Assert.assertEquals(Sets.newHashSet("cdmipMask","image"),(new ColorDepthMask()).getSearchTypes());
        Assert.assertEquals(Sets.newHashSet("sample"),(new Sample()).getSearchTypes());
        Assert.assertEquals(Sets.newHashSet("treeNode"),(new TreeNode()).getSearchTypes());
        Assert.assertEquals(Sets.newHashSet("n5Container","syncedPath"),(new N5Container()).getSearchTypes());
    }
}

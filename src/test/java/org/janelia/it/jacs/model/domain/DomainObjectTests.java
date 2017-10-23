package org.janelia.it.jacs.model.domain;

import org.janelia.it.jacs.model.domain.compartments.CompartmentSet;
import org.janelia.it.jacs.model.domain.gui.alignment_board.AlignmentBoard;
import org.janelia.it.jacs.model.domain.gui.alignment_board.AlignmentContext;
import org.janelia.it.jacs.model.domain.gui.search.Filter;
import org.janelia.it.jacs.model.domain.ontology.Annotation;
import org.janelia.it.jacs.model.domain.ontology.Ontology;
import org.janelia.it.jacs.model.domain.sample.CuratedNeuron;
import org.janelia.it.jacs.model.domain.sample.DataSet;
import org.janelia.it.jacs.model.domain.sample.Image;
import org.janelia.it.jacs.model.domain.sample.LSMImage;
import org.janelia.it.jacs.model.domain.sample.LineRelease;
import org.janelia.it.jacs.model.domain.sample.NeuronFragment;
import org.janelia.it.jacs.model.domain.sample.Sample;
import org.janelia.it.jacs.model.domain.screen.FlyLine;
import org.janelia.it.jacs.model.domain.workspace.TreeNode;
import org.janelia.it.jacs.model.domain.workspace.Workspace;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the base classes of the domain object model.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainObjectTests {

    @Test
    public void testGetType() throws Exception {
        Assert.assertEquals("Alignment Board",(new AlignmentBoard()).getType());
        Assert.assertEquals("Alignment Context", (new AlignmentContext()).getType());
        Assert.assertEquals("Annotation",(new Annotation()).getType());
        Assert.assertEquals("Compartment Set",(new CompartmentSet()).getType());
        Assert.assertEquals("Data Set",(new DataSet()).getType());
        Assert.assertEquals("Filter",(new Filter()).getType());
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
    public void testGetSearchType() throws Exception {
        Assert.assertEquals(null,(new AlignmentBoard()).getSearchType());
        Assert.assertEquals(null,(new Annotation()).getSearchType());
        Assert.assertEquals(null,(new CompartmentSet()).getSearchType());
        Assert.assertEquals("dataSet",(new DataSet()).getSearchType());
        Assert.assertEquals("filter",(new Filter()).getSearchType());
        //Assert.assertEquals("flyLine",(new FlyLine()).getSearchType());
        Assert.assertEquals("image",(new Image()).getSearchType());
        Assert.assertEquals("lsmImage",(new LSMImage()).getSearchType());
        //Assert.assertEquals("release",(new LineRelease()).getSearchType());
        //Assert.assertEquals("fragment",(new NeuronFragment()).getSearchType());
        //Assert.assertEquals("fragment",(new CuratedNeuron()).getSearchType());
        Assert.assertEquals("sample",(new Sample()).getSearchType());
        Assert.assertEquals("treeNode",(new TreeNode()).getSearchType());
        Assert.assertEquals("treeNode",(new Workspace()).getSearchType());
    }
}

package org.janelia.model.domain;

import org.janelia.model.domain.ontology.Category;
import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.domain.ontology.OntologyTerm;
import org.janelia.model.domain.ontology.Tag;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the Ontology domain model.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class OntologyTests {
 
    @Test
    public void testOntologyModelMethods() throws Exception {

        Ontology ontology = new Ontology();
        
        OntologyTerm t1 = new Category();
        t1.setId(1L);
        ontology.addChild(t1);
        
        OntologyTerm t2 = new Tag();
        t2.setName("tag2");
        t2.setId(2L);
        t1.addChild(t2);
        
        OntologyTerm t3 = new Tag();
        t3.setName("tag3");
        t3.setId(3L);
        t1.addChild(t3);
        
        OntologyTerm t4 = new Tag();
        t4.setName("tag2");
        t4.setId(3L);
        t1.addChild(t4);

        Assert.assertTrue(t1.hasChild(t2));
        Assert.assertTrue(t1.hasChild(t3));
        Assert.assertTrue(t1.hasChild(t4));
        Assert.assertEquals(t1, t4.getParent());
        
        Assert.assertEquals(ontology, t1.getOntology());
        Assert.assertEquals(ontology, t2.getOntology());
        Assert.assertEquals(ontology, t3.getOntology());
        Assert.assertEquals(ontology, t4.getOntology());
        
        Assert.assertEquals(t3, ontology.findTerm(3L));
        Assert.assertEquals(t3, t1.findTerm(3L));
        Assert.assertEquals(t3, t3.findTerm(3L));
        
        Assert.assertEquals(t2, ontology.findTerm("tag2"));
        Assert.assertNull(t3.findTerm("tag2"));
    }
}

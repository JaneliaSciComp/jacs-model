package org.janelia.model.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import org.janelia.model.domain.enums.FileType;
import org.janelia.model.domain.enums.SubjectRole;
import org.janelia.model.domain.gui.search.Filter;
import org.janelia.model.domain.gui.search.criteria.AttributeValueCriteria;
import org.janelia.model.domain.interfaces.HasFileGroups;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.domain.sample.FileGroup;
import org.janelia.model.domain.sample.Image;
import org.janelia.model.domain.sample.Image2d;
import org.janelia.model.domain.sample.Image3d;
import org.janelia.model.domain.sample.LSMImage;
import org.janelia.model.domain.sample.LSMSummaryResult;
import org.janelia.model.domain.sample.NeuronFragment;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.sample.SampleProcessingResult;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.domain.workspace.Workspace;
import org.janelia.model.security.Group;
import org.janelia.model.security.Subject;
import org.janelia.model.security.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the DomainUtils utility class.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainUtilsTest {

    @Test
    public void testCloneFilter() {
        Filter filter = new Filter();
        filter.setName("New Filter");
        filter.setSearchString("GMR");
        filter.setSearchClass("Sample");

        AttributeValueCriteria criteria = new AttributeValueCriteria();
        criteria.setAttributeName("age");
        criteria.setValue("A02");
        filter.addCriteria(criteria);
        
        Filter clone = DomainUtils.cloneFilter(filter);
        
        Assert.assertEquals("New Filter", clone.getName());
        Assert.assertEquals("GMR", clone.getSearchString());
        Assert.assertEquals("Sample", clone.getSearchClass());
        Assert.assertEquals(1, clone.getCriteriaList().size());
        AttributeValueCriteria clonedCriteria = (AttributeValueCriteria)clone.getCriteriaList().get(0);
        Assert.assertEquals("age", clonedCriteria.getAttributeName());
        Assert.assertEquals("A02", clonedCriteria.getValue());
    }

    @Test
    public void testGet2dTypeNames() {

        FileGroup group1 = new FileGroup("group1");
        group1.setFilepath("/group1");
        group1.getFiles().put(FileType.AllMip, "allmip.png");
        group1.getFiles().put(FileType.SignalMip, "signalmip.png");

        FileGroup group2 = new FileGroup("group2");
        group2.setFilepath("/group2");
        group2.getFiles().put(FileType.AllMip, "allmip.png");
        group2.getFiles().put(FileType.ReferenceMip, "refmip.png");
        
        LSMSummaryResult result = new LSMSummaryResult();
        result.getGroups().add(group1);
        result.getGroups().add(group2);
        
        Multiset<String> names = DomainUtils.get2dTypeNames((HasFileGroups)result);
        Assert.assertEquals(3, names.elementSet().size());
        Assert.assertEquals(2, names.count("AllMip"));
        Assert.assertEquals(1, names.count("SignalMip"));
        Assert.assertEquals(1, names.count("ReferenceMip"));
    }
    
    @Test
    public void testGetAnnotationsByDomainObjectId() {

        List<Annotation> annotations = new ArrayList<>();
        
        Annotation a1 = new Annotation();
        a1.setTarget(Reference.createFor("c1", 1L));
        annotations.add(a1);
        
        Annotation a2 = new Annotation();
        a2.setTarget(Reference.createFor("c1", 1L));
        annotations.add(a2);
        
        Annotation a3 = new Annotation();
        a3.setTarget(Reference.createFor("c1", 2L));
        annotations.add(a3);
        
        Annotation a4 = new Annotation();
        a4.setTarget(Reference.createFor("c2", 3L));
        annotations.add(a4);

        ListMultimap<Long,Annotation> map = DomainUtils.getAnnotationsByDomainObjectId(annotations);
        Assert.assertEquals(3, map.keySet().size());
        Assert.assertEquals(Arrays.asList(a1, a2), map.get(1L));
        Assert.assertEquals(Arrays.asList(a3), map.get(2L));
        Assert.assertEquals(Arrays.asList(a4), map.get(3L));
    }

    @Test
    public void testGetAttributeValue() throws Exception {

        Sample sample = new Sample();
        sample.setId(23L);
        sample.setAge("A");

        Assert.assertEquals(23L, DomainUtils.getAttributeValue(sample, "id")); 
        Assert.assertEquals("A", DomainUtils.getAttributeValue(sample, "age"));
    }

    @Test
    public void testGetClassNameForSearchType() throws Exception {
        Assert.assertEquals(Sample.class.getSimpleName(), DomainUtils.getClassNameForSearchType("sample"));
        Assert.assertEquals(LSMImage.class.getSimpleName(), DomainUtils.getClassNameForSearchType("lsmImage"));
    }

    @Test
    public void testGetCollectionName() throws Exception {
        Assert.assertEquals("sample", DomainUtils.getCollectionName(Sample.class));
        Assert.assertEquals("image", DomainUtils.getCollectionName(LSMImage.class));
    }

    @Test
    public void testGetCollectionName2() throws Exception {
        Assert.assertEquals("sample", DomainUtils.getCollectionName(new Sample()));
        Assert.assertEquals("image", DomainUtils.getCollectionName(new LSMImage()));
    }
    
    @Test
    public void testGetCollectionName3() throws Exception {
        Assert.assertEquals("sample", DomainUtils.getCollectionName(Sample.class.getSimpleName()));
        Assert.assertEquals("image", DomainUtils.getCollectionName(LSMImage.class.getSimpleName()));
    }

    @Test
    public void testGetCollectionNames() throws Exception {
        Assert.assertTrue(!DomainUtils.getCollectionNames().isEmpty());
        Assert.assertTrue(DomainUtils.getCollectionNames().contains("sample"));
    }

    @Test
    public void testGetDefault3dImageFilePath() throws Exception {
        
        SampleProcessingResult result = new SampleProcessingResult();
        result.setFilepath("/root");
        result.getFiles().put(FileType.LosslessStack, "my.v3dpbd");
        
        Assert.assertEquals("/root/my.v3dpbd", DomainUtils.getDefault3dImageFilePath(result));
        
        result.getFiles().put(FileType.VisuallyLosslessStack, "my.h5j");
        Assert.assertEquals("/root/my.v3dpbd", DomainUtils.getDefault3dImageFilePath(result));

        result.getFiles().remove(FileType.LosslessStack);
        Assert.assertEquals("/root/my.h5j", DomainUtils.getDefault3dImageFilePath(result));
    }

    @Test
    public void testGetFilepath() throws Exception {
        
        SampleProcessingResult result = new SampleProcessingResult();
        result.setFilepath("/root");
        result.getFiles().put(FileType.LosslessStack, "my.v3dpbd");

        Assert.assertEquals("/root/my.v3dpbd", DomainUtils.getFilepath(result, FileType.LosslessStack));           
    }

    @Test
    public void testGetIds() throws Exception {
    
        Sample d1 = new Sample();
        d1.setId(1L);
        Sample d2 = new Sample();
        d2.setId(2L);
        Sample d3 = new Sample();
        d3.setId(3L);
        
        Assert.assertEquals(Arrays.asList(1L, 2L, 3L), DomainUtils.getIds(Arrays.asList(d1, d2, d3)));
    }

    @Test
    public void testGetMapById() throws Exception {

        Sample d1 = new Sample();
        d1.setId(1L);
        Sample d2 = new Sample();
        d2.setId(2L);
        Sample d3 = new Sample();
        d3.setId(3L);
        
        Map<Long,Sample> map = DomainUtils.getMapById(Arrays.asList(d1, d2, d3));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(d1, map.get(1L));
        Assert.assertEquals(d2, map.get(2L));
        Assert.assertEquals(d3, map.get(3L));
    }

    @Test
    public void testGetMapByReference() throws Exception {

        Sample d1 = new Sample();
        d1.setId(1L);
        Sample d2 = new Sample();
        d2.setId(2L);
        Sample d3 = new Sample();
        d3.setId(3L);
        
        Map<Reference,Sample> map = DomainUtils.getMapByReference(Arrays.asList(d1, d2, d3));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals(d1, map.get(Reference.createFor(Sample.class.getSimpleName(), 1L)));
        Assert.assertEquals(d2, map.get(Reference.createFor(Sample.class.getSimpleName(), 2L)));
        Assert.assertEquals(d3, map.get(Reference.createFor(Sample.class.getSimpleName(), 3L)));
    }
    
    @Test
    public void testGetNameFromSubjectKey() throws Exception {
        Assert.assertEquals("joe", DomainUtils.getNameFromSubjectKey("user:joe"));
        Assert.assertEquals("workstation_users", DomainUtils.getNameFromSubjectKey("group:workstation_users"));
    }

    @Test
    public void testGetObjectClassByName() throws Exception {
        Assert.assertEquals(Sample.class, DomainUtils.getObjectClassByName(Sample.class.getName()));
    }

    @Test
    public void testGetObjectClasses() throws Exception {
        HashSet<Class<? extends DomainObject>> classes = Sets.newHashSet(DomainUtils.getObjectClasses(Image.class));
        Assert.assertTrue(classes.contains(Image.class));
        Assert.assertTrue(classes.contains(Image2d.class));
        Assert.assertTrue(classes.contains(Image3d.class));
        Assert.assertTrue(classes.contains(LSMImage.class));
    }

    @Test
    public void testGetAllObjectClasses() throws Exception {
        Set<Class<? extends DomainObject>> objectClasses = DomainUtils.getObjectClasses(DomainObject.class);
        Assert.assertTrue(objectClasses.contains(TreeNode.class));
        Assert.assertTrue(objectClasses.contains(Workspace.class));
        Assert.assertTrue(objectClasses.contains(Sample.class));
        Assert.assertTrue(objectClasses.contains(NeuronFragment.class));
    }
    
    @Test
    public void testGetObjectClasses2() throws Exception {
        HashSet<Class<? extends DomainObject>> classes = Sets.newHashSet(DomainUtils.getObjectClasses("image"));
        Assert.assertTrue(classes.contains(Image.class));
        Assert.assertTrue(classes.contains(Image2d.class));
        Assert.assertTrue(classes.contains(Image3d.class));
        Assert.assertTrue(classes.contains(LSMImage.class));
    }

    @Test
    public void testGetReferences() throws Exception {

        Sample d1 = new Sample();
        d1.setId(1L);
        Sample d2 = new Sample();
        d2.setId(2L);
        Sample d3 = new Sample();
        d3.setId(3L);
        
        List<Reference> refs = DomainUtils.getReferences(Arrays.asList(d1, d2, d3));
        Assert.assertEquals(Reference.createFor(d1), refs.get(0));
        Assert.assertEquals(Reference.createFor(d2), refs.get(1));
        Assert.assertEquals(Reference.createFor(d3), refs.get(2));
    }

    @Test
    public void testGetSearchAttributes() throws Exception {
        
        List<DomainObjectAttribute> attrs = DomainUtils.getSearchAttributes(Sample.class);
        Assert.assertFalse(attrs.isEmpty());

        DomainObjectAttribute ageAttr = null;
        for(DomainObjectAttribute attr : attrs) {
            if (attr.getName().equals("age")) {
                ageAttr = attr;
            }
        }
        
        Assert.assertNotNull(ageAttr);
        Assert.assertEquals("Age", ageAttr.getLabel());
        
        Sample sample = new Sample();
        sample.setAge("A");

        Assert.assertEquals("A", ageAttr.getGetter().invoke(sample));
        ageAttr.getSetter().invoke(sample, "B");
        Assert.assertEquals("B", sample.getAge());
    }

    @Test
    public void testGetSearchClasses() throws Exception {
        Assert.assertFalse(DomainUtils.getSearchClasses().isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetSubClasses() throws Exception {
        Set<Class<? extends DomainObject>> subClasses = DomainUtils.getSubClasses(Image.class);
        Assert.assertTrue(subClasses.contains(Image2d.class));
        Assert.assertTrue(subClasses.contains(Image3d.class));
        Assert.assertTrue(subClasses.contains(LSMImage.class));
    }

    @Test
    public void testGetTypeFromSubjectKey() throws Exception {
        Assert.assertEquals("group", DomainUtils.getTypeFromSubjectKey(SubjectRole.WorkstationUsers.getRole()));
    }
    
    @Test
    public void testHasReadAccess() throws Exception {
        Sample sample = new Sample();
        sample.getReaders().add("user:joe");
        Assert.assertTrue(DomainUtils.hasReadAccess(sample, "user:joe"));
    }
    
    @Test
    public void testHasWriteAccess() throws Exception {
        Sample sample = new Sample();
        sample.getWriters().add("user:joe");
        Assert.assertTrue(DomainUtils.hasWriteAccess(sample, "user:joe"));
    }

    @Test
    public void testIsOwner() throws Exception {
        Sample sample = new Sample();
        sample.setOwnerKey("user:joe");
        Assert.assertTrue(DomainUtils.isOwner(sample, "user:joe"));
    }

    @Test
    public void testSetFilepath() throws Exception {
        SampleProcessingResult result = new SampleProcessingResult();
        result.setFilepath("/root");
        DomainUtils.setFilepath(result, FileType.LosslessStack, "my.v3dpbd");
        Assert.assertEquals("/root/my.v3dpbd", DomainUtils.getFilepath(result, FileType.LosslessStack));
    }

    @Test
    public void testSortSubjects() throws Exception {
        
        Subject s1 = new User();
        s1.setKey("user:joe");
        s1.setFullName("Joe User");

        Subject s2 = new User();
        s2.setKey("user:alice");
        s2.setFullName("Alice User");

        Subject s3 = new Group();
        s3.setKey("group:users");
        s3.setFullName("User Group");
        
        List<Subject> subjects = Arrays.asList(s1, s2, s3);
        DomainUtils.sortSubjects(subjects);
        
        Assert.assertEquals(Arrays.asList(s3, s2, s1), subjects);
    }    
    
}

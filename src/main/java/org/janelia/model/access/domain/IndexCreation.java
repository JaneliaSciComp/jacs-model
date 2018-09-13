package org.janelia.model.access.domain;

import org.janelia.model.domain.DomainObjectLock;
import org.janelia.model.domain.ontology.Annotation;
import org.janelia.model.domain.ontology.Ontology;
import org.janelia.model.domain.workspace.TreeNode;
import org.janelia.model.security.Subject;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensure all necessary indexes are created on all MongoDB collections.
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class IndexCreation {

    private static final Logger log = LoggerFactory.getLogger(IndexCreation.class);

    private static final int SAMPLE_LOCK_EXPIRATION_SECONDS = 43200; // 12 hours

    protected DomainDAO dao;

    public IndexCreation(DomainDAO dao) {
        this.dao = dao;
    }

    /**
     * Ensures that all collections are indexed in appropriate ways.
     */
    public void ensureIndexes()  {

        long start = System.currentTimeMillis();
        log.info("Ensuring indexes");

        // Core Model (Shared)

        MongoCollection subjectCollection = dao.getCollectionByClass(Subject.class);
        subjectCollection.ensureIndex("{key:1}","{unique:true}");
        subjectCollection.ensureIndex("{name:1}");
        subjectCollection.ensureIndex("{userGroupRoles.groupKey:1}"); // Support query to get all subjects in a given group

        MongoCollection treeNodeCollection = dao.getCollectionByClass(TreeNode.class);
        ensureDomainIndexes(treeNodeCollection);

        MongoCollection ontologyCollection = dao.getCollectionByClass(Ontology.class);
        ensureDomainIndexes(ontologyCollection);

        MongoCollection annotationCollection = dao.getCollectionByClass(Annotation.class);
        ensureDomainIndexes(annotationCollection);
        annotationCollection.ensureIndex("{target:1,readers:1}");

        MongoCollection objectLockCollection = dao.getCollectionByClass(DomainObjectLock.class);
        objectLockCollection.ensureIndex("{creationDate:1}", "{expireAfterSeconds:"+SAMPLE_LOCK_EXPIRATION_SECONDS+"}");
        objectLockCollection.ensureIndex("{sampleRef:1}", "{unique:true}");
        objectLockCollection.ensureIndex("{ownerKey:1,taskId:1,sampleRef:1}");

        // Fly Model

        MongoCollection alignmentBoardCollection = dao.getCollectionByName("alignmentBoard");
        ensureDomainIndexes(alignmentBoardCollection);

        MongoCollection compartmentSetCollection = dao.getCollectionByName("compartmentSet");
        ensureDomainIndexes(compartmentSetCollection);

        MongoCollection dataSetCollection = dao.getCollectionByName("dataSet");
        ensureDomainIndexes(dataSetCollection);
        dataSetCollection.ensureIndex("{identifier:1}","{unique:true}");
        dataSetCollection.ensureIndex("{pipelineProcesses:1}");

        MongoCollection flyLineCollection = dao.getCollectionByName("flyLine");
        ensureDomainIndexes(flyLineCollection);

        MongoCollection fragmentCollection = dao.getCollectionByName("fragment");
        ensureDomainIndexes(fragmentCollection);
        fragmentCollection.ensureIndex("{separationId:1,readers:1}");
        fragmentCollection.ensureIndex("{sampleRef:1,readers:1}");

        MongoCollection imageCollection = dao.getCollectionByName("image");
        ensureDomainIndexes(imageCollection);
        imageCollection.ensureIndex("{sageId:1}");
        imageCollection.ensureIndex("{slideCode:1}");
        imageCollection.ensureIndex("{filepath:1}");
        imageCollection.ensureIndex("{sampleRef:1,readers:1}");

        MongoCollection sampleCollection = dao.getCollectionByName("sample");
        ensureDomainIndexes(sampleCollection);
        sampleCollection.ensureIndex("{dataSet:1}");

        MongoCollection sampleLockCollection = dao.getCollectionByName("sampleLock");
        sampleLockCollection.ensureIndex("{creationDate:1}", "{expireAfterSeconds:"+SAMPLE_LOCK_EXPIRATION_SECONDS+"}");
        sampleLockCollection.ensureIndex("{sampleRef:1}", "{unique:true}");
        sampleLockCollection.ensureIndex("{ownerKey:1,taskId:1,sampleRef:1}");

        MongoCollection serviceCollection = dao.getCollectionByName("containerizedService");
        ensureDomainIndexes(serviceCollection);
        serviceCollection.ensureIndex("{name:1}");
        
        // Mouse Model

        MongoCollection tmSampleCollection = dao.getCollectionByName("tmSample");
        ensureDomainIndexes(tmSampleCollection);

        MongoCollection tmWorkspaceCollection = dao.getCollectionByName("tmWorkspace");
        ensureDomainIndexes(tmWorkspaceCollection);
        subjectCollection.ensureIndex("{sampleRef:1,readers:1}");

        MongoCollection tmNeuronCollection = dao.getCollectionByName("tmNeuron");
        ensureDomainIndexes(tmNeuronCollection);

        log.info("Indexing MongoDB took " + (System.currentTimeMillis() - start) + " ms");
    }

    private void ensureDomainIndexes(MongoCollection mc) {
        // Compound indexes allow for query on any prefix, so we don't need separate indexes on readers and writers
        mc.ensureIndex("{ownerKey:1,_id:1}");
        mc.ensureIndex("{writers:1,_id:1}");
        mc.ensureIndex("{readers:1,_id:1}");
        mc.ensureIndex("{name:1}");
    }
}

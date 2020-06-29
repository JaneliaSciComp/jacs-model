package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import org.janelia.model.access.domain.dao.DomainDAOManager;
import org.janelia.model.access.domain.dao.mongo.mongodbutils.MongoDBHelper;
import org.janelia.model.access.domain.dao.mongo.mongodbutils.MongoModule;
import org.janelia.model.access.domain.dao.mongo.mongodbutils.RegistryHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class AbstractMongoDaoTest {
    private static MongoClient testMongoClient;
    static MongoDatabase testMongoDatabase;
    static ObjectMapper testObjectMapper;

    @BeforeClass
    public static void setUpMongoClient() {
        testObjectMapper = new ObjectMapper().registerModule(new MongoModule());
        testMongoClient = MongoDBHelper.createMongoClient(
                null,
                DomainDAOManager.DATABASE_HOST,
                null,
                null,
                null,
                null,
                false,
                0, // use default
                0, // use default
                -1, // use default
                0,
                0,
                0,
                () -> RegistryHelper.createCodecRegistryWithJacsksonEncoder(testObjectMapper)
        );
        testMongoDatabase = MongoDBHelper.createMongoDatabase(testMongoClient, DomainDAOManager.DATABASE_NAME);
    }

    @AfterClass
    public static void dropTestDatabase() {
        if (testMongoDatabase != null) testMongoDatabase.drop();
    }

}

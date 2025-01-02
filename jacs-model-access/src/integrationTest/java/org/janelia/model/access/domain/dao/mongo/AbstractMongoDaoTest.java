package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.janelia.model.access.domain.dao.ITestDomainDAOManager;
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
                ITestDomainDAOManager.DATABASE_HOST,
                ITestDomainDAOManager.AUTH_DATABASE_NAME,
                ITestDomainDAOManager.DATABASE_USER,
                ITestDomainDAOManager.DATABASE_PASSWORD,
                ITestDomainDAOManager.REPLICA_SET,
                false,
                0,
                -1,
                0,
                0,
                0,
                () -> RegistryHelper.createCodecRegistryWithJacsksonEncoder(testObjectMapper)
        );
        testMongoDatabase = MongoDBHelper.createMongoDatabase(testMongoClient, ITestDomainDAOManager.DATABASE_NAME);
    }

    @AfterClass
    /**
     * Drop the database after testing
     */
    public static void dropTestDatabase() {
        if (testMongoDatabase != null) testMongoDatabase.drop();
    }

}

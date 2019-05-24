package org.janelia.model.access.domain.dao.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

import org.janelia.model.access.domain.dao.DomainDAOManager;
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
        MongoClientOptions.Builder optionsBuilder = MongoClientOptions
                .builder()
                .codecRegistry(RegistryHelper.createCodecRegistryWithJacsksonEncoder(testObjectMapper))
                ;
        MongoClientURI mongoConnectionURI = new MongoClientURI(
                "mongodb://" + DomainDAOManager.DATABASE_HOST,
                optionsBuilder
        );
        testMongoClient = new MongoClient(mongoConnectionURI);
        testMongoDatabase = testMongoClient.getDatabase(DomainDAOManager.DATABASE_NAME);
    }

    @AfterClass
    public static void dropTestDatabase() {
        if (testMongoDatabase != null) testMongoDatabase.drop();
    }

}

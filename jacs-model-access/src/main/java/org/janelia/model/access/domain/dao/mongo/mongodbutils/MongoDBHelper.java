package org.janelia.model.access.domain.dao.mongo.mongodbutils;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Provider;

import com.google.common.base.Splitter;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MongoDBHelper.class);

    public static MongoClient createMongoClient(
            String mongoConnectionURL,
            String mongoServer,
            String mongoAuthDatabase,
            String mongoUsername,
            String mongoPassword,
            int threadsAllowedToBlockMultiplier,
            int connectionsPerHost,
            int connectTimeout,
            int maxWaitTimeInSecs,
            int maxConnectionIdleTimeInSecs,
            int maxConnLifeTimeInSecs,
            Provider<CodecRegistry> codecRegistryProvider) {

        MongoClientOptions.Builder optionsBuilder =
                MongoClientOptions.builder()
                        .maxWaitTime(maxWaitTimeInSecs * 1000)
                        .maxConnectionIdleTime(maxConnectionIdleTimeInSecs * 1000)
                        .maxConnectionLifeTime(maxConnLifeTimeInSecs * 1000)
                        ;
        if (threadsAllowedToBlockMultiplier > 0) {
            optionsBuilder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockMultiplier);
        }
        if (connectionsPerHost > 0) {
            optionsBuilder.connectionsPerHost(connectionsPerHost);
        }
        if (connectTimeout >= 0) {
            optionsBuilder.connectTimeout(connectTimeout);
        }
        CodecRegistry codecRegistry = codecRegistryProvider.get();
        if (codecRegistry != null) {
            optionsBuilder.codecRegistry(codecRegistry);
        }

        if (StringUtils.isNotBlank(mongoServer)) {
            // Alternative connection method to support passwords special characters not supported by MongoClientURI
            List<ServerAddress> members = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(mongoServer)
                    .stream()
                    .map(ServerAddress::new)
                    .collect(Collectors.toList());
            if (StringUtils.isNotBlank(mongoUsername)) {
                char[] passwordChars = StringUtils.isBlank(mongoPassword) ? null : mongoPassword.toCharArray();
                MongoCredential credential = MongoCredential.createCredential(mongoUsername, mongoAuthDatabase, passwordChars);
                MongoClient m = new MongoClient(members, credential, optionsBuilder.build());
                LOG.info("Connected to MongoDB ({}@{}) as user {}", mongoAuthDatabase, mongoServer, mongoUsername);
                return m;
            } else {
                MongoClient m = new MongoClient(members, optionsBuilder.build());
                LOG.info("Connected to MongoDB server {}", mongoServer);
                return m;
            }
        } else {
            // use connection URL
            if (StringUtils.isBlank(mongoConnectionURL)) {
                LOG.error("Neither mongo server(s) nor the mongo URL have been specified");
                throw new IllegalStateException("Neither mongo server(s) nor the mongo URL have been specified");
            } else {
                MongoClientURI mongoConnectionString = new MongoClientURI(mongoConnectionURL, optionsBuilder);
                MongoClient m = new MongoClient(mongoConnectionString);
                LOG.info("Connected to MongoDB {}", mongoConnectionString);
                return m;
            }
        }
    }

    public static MongoDatabase createMongoDatabase(MongoClient mongoClient, String mongoDatabaseName) {
        LOG.trace("Connecting to database: {}", mongoDatabaseName);
        return mongoClient.getDatabase(mongoDatabaseName);
    }

}

package org.janelia.model.access.domain.dao.mongo.mongodbutils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import jakarta.inject.Provider;

import com.google.common.base.Splitter;
import com.mongodb.MongoClientOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.configuration.CodecRegistries;
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
            String mongoReplicaSet,
            boolean useSSL,
            int connectionsPerHost,
            int connectTimeoutInMillis,
            int maxWaitTimeInSecs,
            int maxConnectionIdleTimeInSecs,
            int maxConnLifeTimeInSecs,
            Provider<CodecRegistry> codecRegistryProvider) {
        CodecRegistry codecRegistry = codecRegistryProvider.get();
        MongoClientSettings.Builder mongoClientSettingsBuilder = MongoClientSettings.builder()
                .codecRegistry(codecRegistry == null
                        ? MongoClientSettings.getDefaultCodecRegistry()
                        : CodecRegistries.fromRegistries(
                            MongoClientSettings.getDefaultCodecRegistry(),
                            codecRegistry))
                .writeConcern(WriteConcern.JOURNALED)
                .applyToConnectionPoolSettings(builder -> {
                            if (connectionsPerHost > 0) {
                                builder.maxSize(connectionsPerHost);
                            }
                            if (maxWaitTimeInSecs > 0) {
                                builder.maxWaitTime(maxWaitTimeInSecs, TimeUnit.SECONDS);
                            }
                            if (maxConnectionIdleTimeInSecs > 0) {
                                builder.maxConnectionIdleTime(maxConnectionIdleTimeInSecs, TimeUnit.SECONDS);
                            }
                            if (maxConnLifeTimeInSecs > 0) {
                                builder.maxConnectionLifeTime(maxConnLifeTimeInSecs, TimeUnit.SECONDS);
                            }
                        })
                .applyToSocketSettings(builder -> {
                    if (connectTimeoutInMillis > 0) {
                        builder.connectTimeout(connectTimeoutInMillis, TimeUnit.MILLISECONDS);
                    }
                })
                .applyToSslSettings(builder -> builder.enabled(useSSL))
                ;
        if (StringUtils.isNotBlank(mongoServer)) {
            List<ServerAddress> clusterMembers = Splitter.on(',')
                    .trimResults().omitEmptyStrings()
                    .splitToList(mongoServer).stream()
                    .map(ServerAddress::new)
                    .collect(Collectors.toList());
            LOG.info("Connect to {}", clusterMembers);
            mongoClientSettingsBuilder.applyToClusterSettings(builder -> builder.hosts(clusterMembers));
        } else {
            // use connection URL
            if (StringUtils.isBlank(mongoConnectionURL)) {
                LOG.error("Neither mongo server(s) nor the mongo URL have been specified");
                throw new IllegalStateException("Neither mongo server(s) nor the mongo URL have been specified");
            } else {
                mongoClientSettingsBuilder.applyConnectionString(new ConnectionString(mongoConnectionURL));
            }
        }
        if (StringUtils.isNotBlank(mongoReplicaSet)) {
            LOG.info("Use replica set: {}", mongoReplicaSet);
            mongoClientSettingsBuilder.applyToClusterSettings(builder -> builder.requiredReplicaSetName(mongoReplicaSet));
        }
        if (StringUtils.isNotBlank(mongoUsername)) {
            LOG.info("Authenticate to MongoDB ({}@{})", mongoAuthDatabase, StringUtils.defaultIfBlank(mongoServer, mongoConnectionURL),
                    StringUtils.isBlank(mongoUsername) ? "" : " as user " + mongoUsername);
            char[] passwordChars = StringUtils.isBlank(mongoPassword) ? null : mongoPassword.toCharArray();
            mongoClientSettingsBuilder.credential(MongoCredential.createCredential(mongoUsername, mongoAuthDatabase, passwordChars));
        }
        return MongoClients.create(mongoClientSettingsBuilder.build());
    }

    public static MongoDatabase createMongoDatabase(MongoClient mongoClient, String mongoDatabaseName) {
        return mongoClient.getDatabase(mongoDatabaseName);
    }

    public static com.mongodb.MongoClient createLegacyMongoClient(
            String mongoConnectionURL,
            String mongoServer,
            String mongoAuthDatabase,
            String mongoUsername,
            String mongoPassword,
            boolean useSSL,
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
                        .sslEnabled(useSSL)
                ;
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
                com.mongodb.MongoClient m = new com.mongodb.MongoClient(members, credential, optionsBuilder.build());
                LOG.info("Connected to MongoDB ({}@{}) as user {}", mongoAuthDatabase, mongoServer, mongoUsername);
                return m;
            } else {
                com.mongodb.MongoClient m = new com.mongodb.MongoClient(members, optionsBuilder.build());
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
                com.mongodb.MongoClient m = new com.mongodb.MongoClient(mongoConnectionString);
                LOG.info("Connected to MongoDB {}", mongoConnectionString);
                return m;
            }
        }
    }

}

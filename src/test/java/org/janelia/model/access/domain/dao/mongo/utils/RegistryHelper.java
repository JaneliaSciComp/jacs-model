package org.janelia.model.access.domain.dao.mongo.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

public class RegistryHelper {

    public static CodecRegistry createCodecRegistry(ObjectMapper objectMapper) {
        return CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromProviders(new JacksonCodecProvider(objectMapper))
        );
    }

}

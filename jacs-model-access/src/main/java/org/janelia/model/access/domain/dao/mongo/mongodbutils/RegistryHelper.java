package org.janelia.model.access.domain.dao.mongo.mongodbutils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

public class RegistryHelper {

    public static CodecRegistry createCodecRegistry() {
        return CodecRegistries.fromRegistries(
                MongoClient.getDefaultCodecRegistry(),
                CodecRegistries.fromCodecs(
                        new ReferenceCodec(),
                        new BigIntegerCodec(),
                        new ClassCodec()
                ),
                CodecRegistries.fromProviders(new EnumCodecProvider())
        );
    }

    public static CodecRegistry createCodecRegistryWithJacsksonEncoder(ObjectMapper objectMapper) {
        return CodecRegistries
                .fromRegistries(
                        createCodecRegistry(),
                        CodecRegistries.fromProviders(new JacksonCodecProvider(objectMapper))
                );
    }
}

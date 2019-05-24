package org.janelia.model.access.domain.dao.mongo.mongodbutils;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class EnumCodecProvider implements CodecProvider {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz.isEnum()) {
            return (Codec<T>) new EnumCodec(clazz);
        }
        return null;
    }
}

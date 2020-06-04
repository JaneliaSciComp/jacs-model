package org.janelia.model.access.domain.dao.mongo.mongodbutils;

import org.apache.commons.lang3.StringUtils;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.janelia.model.domain.Reference;

/**
 * ClassCodec implements a Codec for a Class type.
 */
public class ClassCodec implements Codec<Class<?>> {

    @Override
    public Class<?> decode(BsonReader reader, DecoderContext decoderContext) {
        String value = reader.readString();
        if (StringUtils.isBlank(value)) {
            return null;
        } else {
            try {
                return Class.forName(value);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

    @Override
    public void encode(BsonWriter writer, Class<?> value, EncoderContext encoderContext) {
        if (value == null) {
            writer.writeNull();
        } else {
            writer.writeString(value.getName());
        }
    }

    @Override
    public Class getEncoderClass() {
        return Class.class;
    }
}

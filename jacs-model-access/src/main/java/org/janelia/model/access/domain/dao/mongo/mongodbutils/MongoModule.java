package org.janelia.model.access.domain.dao.mongo.mongodbutils;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Date;

public class MongoModule extends SimpleModule {

    public MongoModule() {
        setNamingStrategy(new MongoNamingStrategy());
        addSerializer(Date.class, new ISODateSerializer());
        addDeserializer(Date.class, new ISODateDeserializer());
        addDeserializer(Long.class, new MongoNumberLongDeserializer());
        addDeserializer(Double.class, new MongoNumberDoubleDeserializer());
        addDeserializer(Number.class, new MongoNumberDeserializer());
    }

}

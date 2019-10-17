package org.janelia.rendering.ymlrepr;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class RawVolReader {

    public RawVolData readRawVolData(InputStream rawVolStream) throws IOException {
        ObjectMapper ymlReader = new ObjectMapper(new YAMLFactory());
        if (rawVolStream == null) {
            return null;
        }
        return ymlReader.readValue(rawVolStream, RawVolData.class);
    }

}

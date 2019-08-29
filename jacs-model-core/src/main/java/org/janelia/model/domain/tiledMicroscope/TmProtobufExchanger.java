package org.janelia.model.domain.tiledMicroscope;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exchanges data between byte array and RawTile Microscope.  At time of writing,
 * all contents of a TmNeuronData are supported.
 * <p>
 * All operations here must be thread safe.
 *
 * @author fosterl
 */
public class TmProtobufExchanger {

    private static final Logger LOG = LoggerFactory.getLogger(TmProtobufExchanger.class);

    private Schema<TmNeuronData> schema = null;

    public TmProtobufExchanger() {
        try {
            schema = RuntimeSchema.getSchema(TmNeuronData.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to get schema for " + TmNeuronData.class.getCanonicalName(), ex);
        }
    }

    public void deserializeNeuron(InputStream protobufData, TmNeuronMetadata tmNeuronMetadata) throws Exception {
        final LinkedBuffer buffer = LinkedBuffer.allocate();
        try {
            TmNeuronData neuronData = new TmNeuronData();
            ProtobufIOUtil.mergeFrom(protobufData, neuronData, schema);
            tmNeuronMetadata.setNeuronData(neuronData);
            LOG.trace("Deserialized neuron data for TmNeuronMetadata#{}\nDeserializing: {}", tmNeuronMetadata.getId(), tmNeuronMetadata.getDebugString());
            tmNeuronMetadata.initNeuronData();
        } finally {
            buffer.clear();
        }
    }

    public byte[] serializeNeuron(TmNeuronMetadata tmNeuronMetadata) throws Exception {
        LOG.trace("Serializing neuron data for TmNeuronMetadata#{}\nSerializing: {}", tmNeuronMetadata.getId(), tmNeuronMetadata.getDebugString());
        return serializeNeuron(tmNeuronMetadata.getNeuronData());
    }

    /**
     * Turn a neuron into a series of bytes.
     *
     * @param neuronData what to store
     * @return array of bytes, suitable for
     * @throws Exception from any called methods.
     */
    private byte[] serializeNeuron(TmNeuronData neuronData) throws Exception {
        if (neuronData == null) throw new IllegalArgumentException("Neuron data is null");

        // Populate a byte array from serialized data.

        // NOTE: there is an occasional sporadic concurrent modification
        // exception thrown my io.protostuff.MapSchema, line 341. It looks
        // like there might be a bug in the code whereby the collection is
        // modifying itself. To deal with this, a re-try loop is given
        // here.

        int retries = 5;
        byte[] protobuf = null;

        for (; retries > 0; retries--) {
            try {
                protobuf = null;
                final LinkedBuffer buffer = LinkedBuffer.allocate();
                if (retries < 5) {
                    LOG.info("Retry {} serializeNeuron - starting with {} retries left", 5 - retries, retries);
                }
                try {
                    protobuf = ProtobufIOUtil.toByteArray(neuronData, schema, buffer);
                } finally {
                    buffer.clear();
                }
                if (protobuf != null) {
                    break;
                }
            } catch (Throwable t) {
                LOG.warn("serializeNeuron failed: {}, retries left={}", t.getMessage(), retries);
            }
            Thread.sleep(5);
        }
        if (protobuf == null) {
            throw new Exception("serializeNeuron failed and exhausted all retries");
        }
        return protobuf;
    }

    public void copyNeuronData(TmNeuronMetadata source, TmNeuronMetadata target) {
        target.setNeuronData(source.getNeuronData());
    }

    public TmNeuronData getNeuronData(TmNeuronMetadata tmNeuronMetadata) {
        return tmNeuronMetadata.getNeuronData();
    }
}

package org.janelia.model.access.tiledMicroscope;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.janelia.model.domain.tiledMicroscope.TmNeuronMetadata;
import org.janelia.model.domain.tiledMicroscope.TmWorkspace;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Asynchronous API for updating neuron save in the database.
 *
 * @author fosterl
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface TmModelAdapter {

    List<TmNeuronMetadata> loadNeurons(TmWorkspace workspace) throws Exception;

    void asyncCreateNeuron(TmNeuronMetadata tmNeuronMetadata) throws Exception;

    void asyncSaveNeuronMetadata(TmNeuronMetadata neuron) throws Exception;

    void asyncSaveNeuron(TmNeuronMetadata neuron) throws Exception;

    void asyncDeleteNeuron(TmNeuronMetadata neuronMetadata) throws Exception;

    CompletableFuture<Boolean> requestOwnership(TmNeuronMetadata neuronMetadata) throws Exception;

    CompletableFuture<Boolean> requestAssignment(TmNeuronMetadata neuronMetadata, String targetUser) throws Exception;

}

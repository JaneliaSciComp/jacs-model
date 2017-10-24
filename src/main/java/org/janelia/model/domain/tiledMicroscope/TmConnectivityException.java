package org.janelia.model.domain.tiledMicroscope;

/**
 * This exception is thrown when the nodes in a TmNeuron are not
 * correctly connected; eg, one node's parent ID does not exist
 * in the neuron
 */
public class TmConnectivityException extends Exception {

    public TmConnectivityException() {
    }

    public TmConnectivityException(String message) {
        super(message);
    }

    public TmConnectivityException(Throwable cause) {
        super(cause);
    }

    public TmConnectivityException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.janelia.model.access.domain;

import java.util.List;

/**
 * IdGenerator
 *
 * @param <I> ID type
 */
public interface IdGenerator<I> {
    I generateId();
    List<I> generateIdList(int n);
}


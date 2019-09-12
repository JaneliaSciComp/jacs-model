package org.janelia.model.domain;

@FunctionalInterface
public interface DataSupplier<K, V> {
    V getData(K key);
}

package org.janelia.configutils;

@FunctionalInterface
public interface ContextValueGetter {
    String get(String key);
}

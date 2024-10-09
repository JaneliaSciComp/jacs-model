package org.janelia.rendering;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class StorageOptions {

    public static StorageOptions combine(StorageOptions so1, StorageOptions so2) {
        if (so2 == null) {
            return so1;
        } else if (so1 == null) {
            return so2;
        } else {
            StorageOptions result = new StorageOptions();
            result.options.putAll(so1.options);
            result.options.putAll(so2.options);
            return result;
        }
    }

    private final Map<String, Object> options = new HashMap<>();

    public Collection<String> getAttributeNames() {
        return options.keySet();
    }

    public Object getAttributeValue(String attributeName) {
        return options.get(attributeName);
    }

    public StorageOptions setFrom(Map<String, Object> options) {
        this.options.clear();
        this.options.putAll(options);
        return this;
    }
}

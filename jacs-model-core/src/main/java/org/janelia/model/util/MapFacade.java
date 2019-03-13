package org.janelia.model.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A special facade class which wraps an existing List and provides a Map-style access to it, based on some custom
 * key function. This class is careful to maintain always update the wrapped list whenever the map is changed.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public abstract class MapFacade<K, V> implements Map<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(MapFacade.class);

    private final List<V> list;
    private final Map<K, V> map = new HashMap<>();

    public MapFacade(List<V> list) {
        this.list = list;
        for(V object : list) {
            map.put(getKey(object), object);
        }
        verifyIntegrity();
    }

    public abstract K getKey(V object);

    private void verifyIntegrity() {
        if (map.size() != list.size()) {
            LOG.warn("Data structures out of sync. Map size {} != list size {}, map: {}, list: {}", map.size(), list.size(), map, list);
            throw new IllegalStateException("Data structures out of sync. Map size (" + map.size() + ") != list size (" + list.size() + ")");
        }
    }

    @Override
    public boolean containsKey(Object key) {
        verifyIntegrity();
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        verifyIntegrity();
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        verifyIntegrity();
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        LOG.trace("MapFacade put {} {}, list is {}, map is {}", key, value);
        verifyIntegrity();
        if (map.containsKey(key)) {
            list.remove(map.remove(key));
        }
        list.add(value);
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        LOG.trace("MapFacade remove {} list is {}, map is {}", key, list, map);
        verifyIntegrity();
        V toRemove = map.get(key);
        list.remove(toRemove);
        return map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        m.forEach((k, v) -> put(k, v));
    }

    @Override
    public void clear() {
        map.clear();
        list.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        verifyIntegrity();
        return map.entrySet();
    }

}

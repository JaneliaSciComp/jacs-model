package org.janelia.it.jacs.model.domain.support;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A view that provides a union of several maps. If multiple maps share the same key, 
 * the value that is associated with that key is undefined.
 *
 * TODO: move this to the shared module
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class MapUnion<K,V> implements Map<K,V> {

    private final Set<Map<K,V>> maps = new LinkedHashSet<>();
    private final Map<K,V> defaultMap = new LinkedHashMap<>();
    
    public MapUnion() {
        maps.add(defaultMap);
    }
    
    public void addMap(Map<K,V> map) {
        maps.add(map);
    }
    
    @Override
    public int size() {
        int c = 0;
        for(Map<K,V> map : maps) {
            c += map.size();
        }
        return c;
    }

    @Override
    public boolean isEmpty() {
        return size()==0;
    }

    @Override
    public boolean containsKey(Object key) {
        for(Map<K,V> map : maps) {
            if (map.containsKey(key)) return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        for(Map<K,V> map : maps) {
            if (map.containsValue(value)) return true;
        }
        return false;
    }

    @Override
    public V get(Object key) {
        for(Map<K,V> map : maps) {
            if (map.containsKey(key)) {
                V value = map.get(key);
                return value;
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) {
        return defaultMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V lastRemoved = null;
        for(Map<K,V> map : maps) {
            V value = map.remove(key);
            if (value!=null) {
                lastRemoved = value;
            }
        }
        return lastRemoved;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        maps.add((Map<K, V>) m);
    }

    @Override
    public void clear() {
        defaultMap.clear();
        maps.clear();
        maps.add(defaultMap);
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}

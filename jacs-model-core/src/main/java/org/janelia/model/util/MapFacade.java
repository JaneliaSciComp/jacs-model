package org.janelia.model.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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

    protected MapFacade(List<V> list) {
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
        list.remove(map.get(key));
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
    public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        map.clear();
        list.clear();
    }

    @Override
    @Nonnull
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    @Nonnull
    public Collection<V> values() {
        return map.values();
    }

    @Override
    @Nonnull
    public Set<Map.Entry<K, V>> entrySet() {
        verifyIntegrity();
        return new EntrySet(map.entrySet());
    }

    private final class EntrySet extends AbstractSet<Entry<K,V>> {

        private Set<Map.Entry<K, V>> mapEntrySet;

        private EntrySet(Set<Map.Entry<K, V>> mapEntrySet) {
            this.mapEntrySet = mapEntrySet;
        }

        public Iterator<Entry<K,V>> iterator() {
            return new EntryIterator(mapEntrySet);
        }

        public boolean contains(Object o) {
            return mapEntrySet.contains(o);
        }

        public boolean remove(Object o) {
            if (mapEntrySet.remove(o)) {
                return list.remove(o);
            }
            return false;
        }

        public int size() {
            return mapEntrySet.size();
        }

        public void clear() {
            mapEntrySet.clear();
            list.clear();
        }
    }

    private class EntryIterator implements Iterator<Map.Entry<K,V>> {

        private Iterator<Map.Entry<K, V>> mapIterator;
        private Map.Entry<K,V> last;

        private EntryIterator(Set<Map.Entry<K, V>> entrySet) {
            this.mapIterator = entrySet.iterator();
        }

        public final boolean hasNext() {
            return mapIterator.hasNext();
        }

        public Map.Entry<K,V> next() {
            this.last = mapIterator.next();
            return last;
        }

        public void remove() {
            mapIterator.remove();
            list.remove(last.getValue());
        }
    }
}

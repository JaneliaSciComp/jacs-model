package org.janelia.it.jacs.model.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
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
public abstract class MapFacade<K, V> extends AbstractMap<K, V> {
    
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
    	if (map.size()!=list.size()) {
    		throw new IllegalStateException("Data structures out of sync. Map size ("+map.size()+") != list size ("+list.size()+")");
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
    	verifyIntegrity();
    	if (map.containsKey(key)) {
    	    list.remove(map.get(key));
    	}
        list.add(value);
        return map.put(key, value);
    }

    @Override
    public V remove(Object key) {
    	verifyIntegrity();
        list.remove(map.get(key));
        return map.remove(key);
    }

    // Views
    private transient Set<Map.Entry<K,V>> entrySet = null;
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        verifyIntegrity();
        Set<Map.Entry<K,V>> es = entrySet;
        return es != null ? es : (entrySet = new EntrySet());
    }
    
    private final class EntrySet extends AbstractSet<Map.Entry<K,V>> {

        private Set<Map.Entry<K, V>> mapEntrySet;
        
        public EntrySet() {
            this.mapEntrySet = map.entrySet();
        }
        
        public Iterator<Map.Entry<K,V>> iterator() {
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
            // This uses the iterator to remove all items, so it keeps the list in sync
            mapEntrySet.clear(); 
        }
    }

    private class EntryIterator implements Iterator<Map.Entry<K,V>> {
        
        private Iterator<Map.Entry<K, V>> mapIterator;
        private Map.Entry<K,V> last;
        
        public EntryIterator(Set<Map.Entry<K, V>> entrySet) {
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
            list.remove(last);
        }
    }

}

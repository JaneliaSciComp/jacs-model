package org.janelia.model.domain;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.janelia.model.domain.enums.AlignmentScoreType;
import org.janelia.model.domain.sample.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a DomainObject which provides a facade with dynamic access to its properties by UI label. This is
 * useful for user-driven user interface configuration.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DynamicDomainObjectProxy implements Map<String,Object> {

    private static final Logger LOG = LoggerFactory.getLogger(DynamicDomainObjectProxy.class);

    private static final LoadingCache<Class<? extends DomainObject>,Map<String,DomainObjectAttribute>> CLASS_ATTR_MAPS =
            CacheBuilder.newBuilder().build(new CacheLoader<Class<? extends DomainObject>, Map<String, DomainObjectAttribute>>() {
                public Map<String, DomainObjectAttribute> load(Class<? extends DomainObject> clazz) {
                    Map<String, DomainObjectAttribute> attrs = new LinkedHashMap<>();
                    LOG.trace("Getting attrs for {}", clazz.getSimpleName());
                    for (DomainObjectAttribute attr : DomainUtils.getDisplayAttributes(clazz)) {
                        LOG.trace("  {} -> {}.{}", attr.getLabel(), clazz.getSimpleName(), attr.getName());
                        attrs.put(attr.getLabel(), attr);
                    }
                    return attrs;
                }
            });

    private final DomainObject domainObject;
    private final Map<String, DomainObjectAttribute> attrs;

    public DynamicDomainObjectProxy(DomainObject domainObject) {
        try {
            this.domainObject = domainObject;
            this.attrs = CLASS_ATTR_MAPS.get(domainObject.getClass());
        }
        catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return attrs.size();
    }

    @Override
    public boolean isEmpty() {
        return attrs.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return attrs.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("This method is not implemented on the proxy object");
    }

    @Override
    public Object get(Object key) {
        DomainObjectAttribute attr = attrs.get(key);
        if (attr==null) {
            LOG.trace("{} has no attribute {}", domainObject, key);
        }
        else if (attr.getGetter()==null) {

            // TODO: factor this out into a separate confocal module 
            if (domainObject instanceof Sample) {
                Sample sample = (Sample) domainObject;
                // Is this key an alignment score type?
                Map<AlignmentScoreType, String> scores = SampleUtils.getLatestAlignmentScores(sample);
                // TODO: add a more efficient lookup for these
                for (AlignmentScoreType alignmentScoreType : AlignmentScoreType.values()) {
                    if (alignmentScoreType.getLabel().equals(key)) {
                        return scores.get(alignmentScoreType);
                    }
                }
            }
            
            LOG.trace("Attribute has no getter: "+key);
        }
        else {
            try {
                return attr.getGetter().invoke(domainObject);
            }
            catch (Exception e) {
                LOG.error("Error getting attribute value for '"+key+"' using getter "+attr.getGetter().getName(), e);
            }
        }
        return null;
    }

    @Override
    public Object put(String key, Object value) {
        DomainObjectAttribute attr = attrs.get(key);
        if (attr==null) {
            LOG.trace("No such attribute: "+key);
        }
        else if (attr.getSetter()==null) {
            LOG.trace("Attribute has no setter: "+key);
        }
        else {
            try {
                return attr.getSetter().invoke(domainObject, value);
            }
            catch (Exception e) {
                LOG.error("Error setting attribute value for '"+key+"' using setter "+attr.getSetter().getName(), e);
            }
        }
        return null;
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("This method is not implemented on the proxy object");
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        throw new UnsupportedOperationException("This method is not implemented on the proxy object");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This method is not implemented on the proxy object");
    }

    @Override
    public Set<String> keySet() {
        return attrs.keySet();
    }

    @Override
    public Collection<Object> values() {
        throw new UnsupportedOperationException("This method is not implemented on the proxy object");
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("This method is not implemented on the proxy object");
    }
}

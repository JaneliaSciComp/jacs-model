package org.janelia.it.jacs.model.domain.support;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.janelia.it.jacs.model.domain.DomainObject;
import org.janelia.it.jacs.model.domain.enums.AlignmentScoreType;
import org.janelia.it.jacs.model.domain.sample.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around a DomainObject which provides a facade with dynamic access to its properties by UI label. This is
 * useful for user-driven user interface configuration.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DynamicDomainObjectProxy implements Map<String,Object> {

    private static final Logger log = LoggerFactory.getLogger(DynamicDomainObjectProxy.class);
    
    private final DomainObject domainObject;
    private final HashMap<String,DomainObjectAttribute> attrs;

    public DynamicDomainObjectProxy(DomainObject domainObject) {
        this.domainObject = domainObject;
        
        synchronized (DynamicDomainObjectProxy.class) {
            Class<? extends DomainObject> clazz = domainObject.getClass();
            log.trace("Getting attrs for {}",clazz.getSimpleName());
            this.attrs = new LinkedHashMap<>();
            for (DomainObjectAttribute attr : DomainUtils.getDisplayAttributes(domainObject.getClass())) {
                log.trace("  {} -> {}.{}",attr.getLabel(), domainObject.getClass().getSimpleName(), attr.getName());
                attrs.put(attr.getLabel(), attr);
            }
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
            log.trace("{} has no attribute {}", domainObject, key);
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
            
            log.trace("Attribute has no getter: "+key);
        }
        else {
            try {
                return attr.getGetter().invoke(domainObject);
            }
            catch (Exception e) {
                log.error("Error getting attribute value for '"+key+"' using getter "+attr.getGetter().getName(), e);
            }
        }
        return null;
    }

    @Override
    public Object put(String key, Object value) {
        DomainObjectAttribute attr = attrs.get(key);
        if (attr==null) {
            log.trace("No such attribute: "+key);
        }
        else if (attr.getSetter()==null) {
            log.trace("Attribute has no setter: "+key);
        }
        else {
            try {
                return attr.getSetter().invoke(domainObject, value);
            }
            catch (Exception e) {
                log.error("Error setting attribute value for '"+key+"' using setter "+attr.getSetter().getName(), e);
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

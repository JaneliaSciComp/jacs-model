package org.janelia.model.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic utility methods.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class Utils {

    public static Map<String, Object> strObjMap(Object... values) {
        Map<String, Object> map = new HashMap<>();
        for (int i=0; i<values.length; i+=2) {
            if (values[i] instanceof String) {
                String key = (String)values[i];
                Object value = values[i+1];
                map.put(key, value);
            }
            else {
                throw new IllegalArgumentException("Value at "+i+" must be a String");
            }
        }
        return map;
    }

}

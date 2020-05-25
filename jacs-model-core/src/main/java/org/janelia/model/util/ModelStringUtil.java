package org.janelia.model.util;

import java.util.Arrays;
import java.util.Collection;

public class ModelStringUtil {

    /**
     * Taken from https://stackoverflow.com/questions/2559759/how-do-i-convert-camelcase-into-human-readable-names-in-java
     * 
     * @param s
     * @return
     */
    public static String splitCamelCase(String s) {
        return s.replaceAll(
           String.format("%s|%s|%s",
              "(?<=[A-Z])(?=[A-Z][a-z])",
              "(?<=[^A-Z])(?=[A-Z])",
              "(?<=[A-Za-z])(?=[^A-Za-z])"
           ),
           " "
        );
     }

    public static boolean areEqual(Object s1, Object s2) {
        if (s1==null) {
            return s2==null;
        }
        return s1.equals(s2);
    }

    /**
     * Returns a comma-delimited string listing the toString() representations of all the objects in the given array.
     * @param objArray objects to list
     * @return comma-delimited string
     */
    public static String getCommaDelimited(Object... objArray) {
        return getCommaDelimited(Arrays.asList(objArray));
    }

    /**
     * Returns a comma-delimited string listing the toString() representations of all the objects in the given collection.
     * @param objs objects to list
     * @return comma-delimited string
     */
    public static String getCommaDelimited(Collection<?> objs) {
        return getCommaDelimited(objs, null);
    }

    /**
     * Returns a comma-delimited string listing the toString() representations of all the objects in the given collection.
     * @param objs objects to list
     * @param maxLength Maximum length of the output string. Outputs longer than this are truncated with an elipses.
     * @return comma-delimited string
     */
    public static String getCommaDelimited(Collection<?> objs, Integer maxLength) {
        if (objs==null) return null;
        StringBuffer buf = new StringBuffer();
        for(Object obj : objs) {
            if (maxLength!=null && buf.length()+3>=maxLength) {
                buf.append("...");
                break;
            }
            if (buf.length()>0) buf.append(", ");
            buf.append(obj.toString());
        }
        return buf.toString();
    }
}

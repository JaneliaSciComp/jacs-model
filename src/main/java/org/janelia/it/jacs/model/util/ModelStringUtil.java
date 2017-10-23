package org.janelia.it.jacs.model.util;

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
}

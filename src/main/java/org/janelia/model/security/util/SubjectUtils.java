package org.janelia.model.security.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility methods for dealing with Subjects (User and Groups)
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SubjectUtils {

    public static String getSubjectName(String subjectNameOrKey) {
        if (StringUtils.isBlank(subjectNameOrKey)) return "";
        if (subjectNameOrKey.contains(":")) {
            String[] s = subjectNameOrKey.split(":");
            if (s.length>1) {
                return s[1];
            }
            else {
                return "";
            }
        }
        return subjectNameOrKey;
    }
}

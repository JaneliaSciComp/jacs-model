package org.janelia.model.security.util;

import org.apache.commons.lang3.StringUtils;
import org.janelia.model.security.Subject;
import org.janelia.model.security.User;

import java.util.HashSet;
import java.util.Set;

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
            } else {
                return "";
            }
        } else {
            return subjectNameOrKey;
        }
    }

    public static boolean subjectIsInGroup(Subject subject, String groupKey) {
        if (subject==null) return false;
        if (subject instanceof User) {
            User user = (User)subject;
            return user.hasGroupRead(groupKey);
        }
        return false;
    }

    public static boolean isAdmin(Subject subject) {
        return subjectIsInGroup(subject, Subject.ADMIN_KEY);
    }

    public static Set<String> getReaderSet(Subject subject) {
        Set<String> set = new HashSet<>();
        if (subject==null) return set;
        set.add(subject.getKey());
        if (subject instanceof User) {
            User user = (User)subject;
            set.addAll(user.getReadGroups());
        }
        return set;
    }

    public static Set<String> getWriterSet(Subject subject) {
        Set<String> set = new HashSet<>();
        if (subject==null) return set;
        set.add(subject.getKey());
        if (subject instanceof User) {
            User user = (User)subject;
            set.addAll(user.getWriteGroups());
        }
        return set;
    }

    public static Set<String> getAdminSet(Subject subject) {
        Set<String> set = new HashSet<>();
        if (subject==null) return set;
        set.add(subject.getKey());
        if (subject instanceof User) {
            User user = (User)subject;
            set.addAll(user.getAdminGroups());
        }
        return set;
    }
}

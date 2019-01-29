package org.janelia.model.access.domain.dao;

import org.janelia.model.security.Subject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for accessing subject info.
 */
public interface SubjectDao extends ReadDao<Subject, Long>, WriteDao<Subject, Long> {
    Subject findByKey(String key);
    Subject findByName(String name);
    Subject findByNameOrKey(String nameOrKey);
    Set<String> getReaderSetByKey(String subjectKey);
    Set<String> getWriterSetByKey(String subjectKey);
    List<Subject> getGroupMembers(String nameOrKey);
    Map<Subject, Number> getGroupMembersCount();
}

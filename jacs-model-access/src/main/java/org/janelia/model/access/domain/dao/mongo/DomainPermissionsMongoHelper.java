package org.janelia.model.access.domain.dao.mongo;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.model.Filters;
import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.janelia.model.access.domain.dao.SubjectDao;
import org.janelia.model.security.Subject;

import javax.inject.Inject;
import java.util.Set;

/**
 * Domain permissions helper.
 */
class DomainPermissionsMongoHelper {
    private final SubjectDao subjectDao;

    @Inject
    DomainPermissionsMongoHelper(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    Set<String> retrieveSubjectReadGroups(String subjectKey) {
        return subjectDao.getReaderSetByKey(subjectKey);

    }

    Set<String> retrieveSubjectWriteGroups(String subjectKey) {
        return subjectDao.getWriterSetByKey(subjectKey);
    }

    Bson createReadPermissionFilterForSubjectKey(String subjectKey) {
        Set<String> readers = retrieveSubjectReadGroups(subjectKey);
        if (CollectionUtils.isEmpty(readers)) {
            // only include entities that have no reader restrictions
            return Filters.and(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.or(
                        Filters.exists("readers", false),
                        Filters.eq("readers", ImmutableList.of())
                    )
            );
        }
        else if (readers.contains(Subject.ADMIN_KEY)) {
            return new Document(); // user is in the admin group so simply ignore the filtering in this case
        }
        else {
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.in("readers", readers)
            );
        }
    }

    Bson createSameGroupReadPermissionFilterForSubjectKey(String subjectKey) {
        Set<String> readers = retrieveSubjectReadGroups(subjectKey);
        if (CollectionUtils.isEmpty(readers)) {
            // only include entities that have no reader restrictions
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.exists("readers", false)
            );
        } else {
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.in("readers", readers)
            );
        }
    }

    Bson createWritePermissionFilterForSubjectKey(String subjectKey) {
        Set<String> writers = retrieveSubjectWriteGroups(subjectKey);
        if (CollectionUtils.isEmpty(writers)) {
            // only include entities that have no reader restrictions
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.exists("writers", false)
            );
        } else if (writers.contains(Subject.ADMIN_KEY)) {
            return new Document(); // user is in the admin group so simply ignore the filtering in this case
        } else {
            return Filters.or(
                    Filters.eq("ownerKey", subjectKey),
                    Filters.in("writers", writers)
            );
        }
    }
}

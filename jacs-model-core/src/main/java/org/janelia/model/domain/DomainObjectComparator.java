package org.janelia.model.domain;

import java.util.Comparator;

import org.janelia.model.domain.DomainObject;

import com.google.common.collect.ComparisonChain;
import org.janelia.model.domain.DomainUtils;

/**
 * Intuitive ordering for domain objects, with owned objects first, then by owner, and
 * finally by id (to show most recent last).
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainObjectComparator implements Comparator<DomainObject> {

    private String ownerKey;

    public DomainObjectComparator(String ownerKey) {
        this.ownerKey = ownerKey;
    }

    @Override
    public int compare(DomainObject o1, DomainObject o2) {
        return ComparisonChain.start()
                .compareTrueFirst(isOwner(o1), isOwner(o2))
                .compare(o1.getOwnerKey(), o2.getOwnerKey())
                .compare(o1.getId(), o2.getId()).result();
    }

    private boolean isOwner(DomainObject domainObject) {
        if (ownerKey == null) return false;
        return DomainUtils.isOwner(domainObject, ownerKey);
    }
};

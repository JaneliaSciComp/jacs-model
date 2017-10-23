package org.janelia.it.jacs.model.domain;

import java.net.UnknownHostException;

import org.janelia.it.jacs.model.domain.support.DomainDAO;

/**
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainDAOManager {

    private static DomainDAOManager instance;

    protected DomainDAO dao;

    private DomainDAOManager() {
        try {
            // TODO: in the future we should use a mock database for this
            this.dao = new DomainDAO("dev-mongodb", "jacs");
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static DomainDAOManager getInstance() {
        if (instance==null) {
            instance = new DomainDAOManager();
        }
        return instance;
    }

    public DomainDAO getDao() {
        return dao;
    }

}

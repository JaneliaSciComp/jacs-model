package org.janelia.model.domain;

import java.net.UnknownHostException;

import org.janelia.model.access.domain.DomainDAO;

/**
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainDAOManager {

    private static final String databaseHost = "dev-mongodb";
    private static final String databaseName = "jacs-test";

    private static DomainDAOManager instance;

    protected DomainDAO dao;

    private DomainDAOManager() {
        try {
            // TODO: in the future we should use a mock database for this
            this.dao = new DomainDAO(databaseHost, databaseName);
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

    public void dropTestDatabase() {
        dao.getMongo().getDatabase(databaseName).drop();
    }

}

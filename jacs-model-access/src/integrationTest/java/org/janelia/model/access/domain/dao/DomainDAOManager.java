package org.janelia.model.access.domain.dao;

import org.janelia.model.access.domain.DomainDAO;

/**
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class DomainDAOManager {

    public static final String DATABASE_HOST = "dev-mongodb";
    public static final String DATABASE_NAME = "jacs-test";

    private static DomainDAOManager instance;

    protected DomainDAO dao;

    private DomainDAOManager() {
        // TODO: these should be run as integration tests
        this.dao = new DomainDAO(DATABASE_HOST, DATABASE_NAME);
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
        dao.getMongo().getDatabase(DATABASE_NAME).drop();
    }

}

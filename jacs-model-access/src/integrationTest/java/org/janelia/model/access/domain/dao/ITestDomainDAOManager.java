package org.janelia.model.access.domain.dao;

import org.janelia.model.access.domain.DomainDAO;

/**
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ITestDomainDAOManager {

    public static final String DATABASE_HOST = "dev-mongodb1:27017,dev-mongodb2:27017,dev-mongodb3:27017";
    // DO NOT CHANGE THIS
    // The integration tests (AbstractMongoDaoTest) drop this database after they're done,
    // so if you point this to a real database you're gonna have a bad time.
    public static final String AUTH_DATABASE_NAME = "admin";
    public static final String DATABASE_NAME = "jacs-test";

    public static final String DATABASE_USER = "devAdmin";
    public static final String DATABASE_PASSWORD = "shar3dd3vs3rv3r";
    public static final String REPLICA_SET = "rsDev";
    private static ITestDomainDAOManager instance;

    protected DomainDAO dao;

    private ITestDomainDAOManager() {
        this.dao = new DomainDAO(DATABASE_HOST, AUTH_DATABASE_NAME, DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD);
    }

    public static ITestDomainDAOManager getInstance() {
        if (instance==null) {
            instance = new ITestDomainDAOManager();
        }
        return instance;
    }

    public DomainDAO getDao() {
        return dao;
    }

}

package org.janelia.model.access.domain.dao;

import org.janelia.model.access.domain.DomainDAO;

/**
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ITestDomainDAOManager {

    public static final String DATABASE_HOST = "dev-mongodb";
    // DO NOT CHANGE THIS
    // The integration tests (AbstractMongoDaoTest) drop this database after they're done,
    // so if you point this to a real database you're gonna have a bad time.
    public static final String DATABASE_NAME = "jacs-test";

    private static ITestDomainDAOManager instance;

    protected DomainDAO dao;

    private ITestDomainDAOManager() {
        this.dao = new DomainDAO(DATABASE_HOST, DATABASE_NAME);
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

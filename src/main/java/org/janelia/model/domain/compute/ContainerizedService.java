package org.janelia.model.domain.compute;

import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.support.MongoMapped;

import java.util.ArrayList;
import java.util.List;

/**
 * An ad-hoc service definition based on a Singularity container wrapped by a Java service harness.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="containerizedService",label="Service Definition")
public class ContainerizedService extends AbstractDomainObject {

    private String name;
    private String version;
    private String description;
    private String harnessClass;
    private List<ContainerizedApp> apps = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHarnessClass() {
        return harnessClass;
    }

    public void setHarnessClass(String harnessClass) {
        this.harnessClass = harnessClass;
    }

    public List<ContainerizedApp> getApps() {
        return apps;
    }

    public void setApps(List<ContainerizedApp> apps) {
        if (apps==null) throw new IllegalArgumentException("Property cannot be null");
        this.apps = apps;
    }

    public ContainerizedApp getAppByName(String name) {
        for (ContainerizedApp app : apps) {
            if (name.equals(app.getName())) return app;
        }
        return null;
    }
}

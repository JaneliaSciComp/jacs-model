package org.janelia.model.util;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.vfs.Vfs;

import com.google.common.collect.Lists;

/**
 * Customizes the Reflections utility to avoid the "error reading /System/Library/Java/Extensions/libJ3D.jnilib" issue
 * which happens when running on Mac systems, by nulling various things in the virtual classpath filesystem that is searched. 
 * 
 * This workaround was taken from the Apache Isis project:
 * https://git-wip-us.apache.org/repos/asf?p=isis.git;a=blob;f=core/applib/src/main/java/org/apache/isis/applib/services/classdiscovery/ClassDiscoveryServiceUsingReflections.java;h=283f053ddb15bfe32f111d88891602820854415e;hb=283f053ddb15bfe32f111d88891602820854415e 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class ReflectionsFixer {

    static {
        Vfs.setDefaultURLTypes(getUrlTypes());
    }
    
    public static Reflections getReflections(String packageName, Class<?> clazz) {
        return new Reflections(packageName, ClasspathHelper.forClass(clazz));
    }
    
    public static List<Vfs.UrlType> getUrlTypes() {
        final List<Vfs.UrlType> urlTypes = Lists.newArrayList();
        urlTypes.add(new EmptyIfFileEndingsUrlType(".pom", ".jnilib", "QTJava.zip"));
        urlTypes.addAll(Arrays.asList(Vfs.DefaultUrlTypes.values()));
        return urlTypes;
    }
    
    private static class EmptyIfFileEndingsUrlType implements Vfs.UrlType {

        private final List<String> fileEndings;

        private EmptyIfFileEndingsUrlType(final String... fileEndings) {
            this.fileEndings = Lists.newArrayList(fileEndings);
        }

        @Override
        public boolean matches(URL url) {
            final String protocol = url.getProtocol();
            final String externalForm = url.toExternalForm();
            if (!protocol.equals("file")) {
                return false;
            }
            for (String fileEnding : fileEndings) {
                if (externalForm.endsWith(fileEnding))
                    return true;
            }
            return false;
        }

        @Override
        public Vfs.Dir createDir(final URL url) throws Exception {
            return emptyVfsDir(url);
        }

        private static Vfs.Dir emptyVfsDir(final URL url) {
            return new Vfs.Dir() {
                @Override
                public String getPath() {
                    return url.toExternalForm();
                }

                @Override
                public Iterable<Vfs.File> getFiles() {
                    return Collections.emptyList();
                }

                @Override
                public void close() {
                    //
                }
            };
        }
    }
}

package org.janelia.model.domain.ontology;

import java.util.Set;

/**
 * Simplified annotation representation for the purposes of SOLR indexing.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public class SimpleDomainAnnotation {
    private String tag;
    private Set<String> subjects;

    public SimpleDomainAnnotation(String tag, Set<String> subjects) {
        this.tag = tag;
        this.subjects = subjects;
    }

    public String getTag() {
        return tag;
    }

    public Set<String> getSubjects() {
        return subjects;
    }
}

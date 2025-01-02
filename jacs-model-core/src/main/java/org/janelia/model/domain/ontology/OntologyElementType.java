package org.janelia.model.domain.ontology;

import java.io.Serializable;

/**
 * The type of an ontology element.
 *
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public abstract class OntologyElementType implements Serializable {

    private static String ONTOLOGY_TERM_TYPES_PACKAGE = OntologyTerm.class.getPackage().getName();

    public static OntologyTerm createTypeByName(String className) {

        try {
            return (OntologyTerm)Class.forName(ONTOLOGY_TERM_TYPES_PACKAGE+"."+className).getConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}


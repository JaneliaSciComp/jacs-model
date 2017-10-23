package org.janelia.it.jacs.model.domain.ontology;

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
            return (OntologyTerm)Class.forName(ONTOLOGY_TERM_TYPES_PACKAGE+"."+className).newInstance();
        }
        catch (Exception ex) {
            System.err.println("Could not instantiate term type: "+className);
            ex.printStackTrace();
        }
        return null;
    }
}


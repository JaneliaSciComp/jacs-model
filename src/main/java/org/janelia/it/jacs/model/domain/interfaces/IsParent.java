package org.janelia.it.jacs.model.domain.interfaces;

/**
 * Marks a domain object as a "parent", meaning that it contains or generates children in some way. 
 * The method of child generation is implementation specific, this interface just groups together 
 * all the objects which can be considered "parents".  
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
public interface IsParent {

}

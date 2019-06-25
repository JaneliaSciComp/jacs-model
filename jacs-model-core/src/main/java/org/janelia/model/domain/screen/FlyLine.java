package org.janelia.model.domain.screen;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.janelia.model.domain.AbstractDomainObject;
import org.janelia.model.domain.Reference;
import org.janelia.model.domain.gui.search.Filtering;
import org.janelia.model.domain.gui.search.criteria.AttributeValueCriteria;
import org.janelia.model.domain.gui.search.criteria.Criteria;
import org.janelia.model.domain.gui.search.criteria.FacetCriteria;
import org.janelia.model.domain.sample.Sample;
import org.janelia.model.domain.support.MongoMapped;
import org.janelia.model.domain.support.SearchAttribute;
import org.janelia.model.domain.support.SearchTraversal;

import com.google.common.collect.Sets;

/**
 * A fly line associated with screen data. 
 * 
 * @author <a href="mailto:rokickik@janelia.hhmi.org">Konrad Rokicki</a>
 */
@MongoMapped(collectionName="flyLine",label="Fly Line")
public class FlyLine extends AbstractDomainObject implements Filtering {

    @SearchTraversal({})
    private Reference representativeScreenSample;
    
    @SearchTraversal({})
    private Reference balancedLine;
    
    @SearchTraversal({})
    private Reference originalLine;

    @SearchAttribute(key="robot_id_i",label="Robot Id")
    private Integer robotId;
    
    @SearchAttribute(key="split_part_txt",label="Split Part",facet="split_part_s")
    private String splitPart;

    @JsonIgnore
    private List<Criteria> lazyCriteria;

    public Integer getRobotId() {
        return robotId;
    }
    
	public Reference getRepresentativeScreenSample() {
		return representativeScreenSample;
	}

	public void setRepresentativeScreenSample(Reference representativeScreenSample) {
		this.representativeScreenSample = representativeScreenSample;
	}

	public Reference getBalancedLine() {
		return balancedLine;
	}

	public void setBalancedLine(Reference balancedLine) {
		this.balancedLine = balancedLine;
	}

	public Reference getOriginalLine() {
		return originalLine;
	}

	public void setOriginalLine(Reference originalLine) {
		this.originalLine = originalLine;
	}

	public void setRobotId(Integer robotId) {
        this.robotId = robotId;
    }

    public String getSplitPart() {
        return splitPart;
    }

    public void setSplitPart(String splitPart) {
        this.splitPart = splitPart;
    }

    /* implement Filtering interface */

    @JsonIgnore
    @Override
    public String getSearchClass() {
        return Sample.class.getName();
    }

    @JsonIgnore
    @Override
    public boolean hasCriteria() {
        return true;
    }

    @JsonIgnore
    @Override
    public String getSearchString() {
        return null;
    }

    @JsonIgnore
    @Override
    public List<Criteria> getCriteriaList() {
        if (lazyCriteria==null) {
            lazyCriteria = new ArrayList<>();
            FacetCriteria sageSynced = new FacetCriteria();
            sageSynced.setAttributeName("sageSynced");
            sageSynced.setValues(Sets.newHashSet("true"));
            lazyCriteria.add(sageSynced);
            AttributeValueCriteria line = new AttributeValueCriteria();
            line.setAttributeName("line");
            line.setValue(getName());
            lazyCriteria.add(line);
        }
        return lazyCriteria;
    }
}

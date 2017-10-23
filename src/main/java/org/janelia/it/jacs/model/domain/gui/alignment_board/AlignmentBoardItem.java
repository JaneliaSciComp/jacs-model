package org.janelia.it.jacs.model.domain.gui.alignment_board;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AlignmentBoardItem implements Serializable {

    private String name;
    private AlignmentBoardReference target;
    private boolean visible = true;
    private String inclusionStatus;
    private String color;
    private String renderMethod;
    private List<AlignmentBoardItem> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlignmentBoardReference getTarget() {
        return target;
    }

    public void setTarget(AlignmentBoardReference target) {
        this.target = target;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getInclusionStatus() {
        return inclusionStatus;
    }

    public void setInclusionStatus(String inclusionStatus) {
        this.inclusionStatus = inclusionStatus;
    }

    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }

    public String getRenderMethod() {
        return renderMethod;
    }

    public void setRenderMethod(String renderMethod) {
        this.renderMethod = renderMethod;
    }

    public List<AlignmentBoardItem> getChildren() {
        return children;
    }

    public void setChildren(List<AlignmentBoardItem> children) {
        if (children==null) throw new IllegalArgumentException("Property cannot be null");
        this.children = children;
    }    
}

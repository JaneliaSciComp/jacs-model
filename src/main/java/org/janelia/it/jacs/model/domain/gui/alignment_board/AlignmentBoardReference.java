package org.janelia.it.jacs.model.domain.gui.alignment_board;

import java.io.Serializable;

import org.janelia.it.jacs.model.domain.Reference;

public class AlignmentBoardReference implements Serializable {

    private Reference objectRef;
    private Long itemId;

    public AlignmentBoardReference() {
    }

    public AlignmentBoardReference(Reference objectRef, Long itemId) {
        this.objectRef = objectRef;
        this.itemId = itemId;
    }

    public Reference getObjectRef() {
        return objectRef;
    }

    public void setObjectRef(Reference objectRef) {
        this.objectRef = objectRef;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return "AlignmentBoardReference{" +
                "objectRef=" + objectRef +
                ", itemId=" + itemId +
                '}';
    }
}

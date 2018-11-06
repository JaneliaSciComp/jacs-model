package org.janelia.model.domain.tiledMicroscope;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;

@JsonTypeName("pointlistreview")
public class TmPointListReviewItem implements TmReviewItem {
    private String pathname;
    private List<List<Long>> pointList  = new ArrayList<>();
    private boolean reviewed;

    @Override
    public boolean isReviewed() {
        return reviewed;
    }

    @Override
    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    @Override
    public void setName(String name) {
        pathname = name;
    }

    @Override
    public String getName() {
        return pathname;
    }

    @Override
    public List getReviewItems() {
        return pointList;
    }

    @Override
    public void addReviewItem(Object item) {
        pointList.add((List<Long>)item);

    }
}

package org.janelia.it.jacs.model.domain.orders;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.bson.types.ObjectId;
import org.janelia.it.jacs.model.domain.enums.OrderStatus;
import org.janelia.it.jacs.model.domain.support.MongoMapped;
import org.jongo.marshall.jackson.oid.ObjectIdSerializer;

/**
 * Stores information about orders from TMOG and front-end
 *
 * @author <a href="mailto:schauderd@janelia.hhmi.org">David Schauder</a>
 */
@MongoMapped(collectionName="intakeOrders",label="IntakeOrder")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public class IntakeOrder implements Serializable {
    private ObjectId _id;
    private String orderNo;
    private List<Long> sampleIds;
    private OrderStatus status;

    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssX")
    private Date startDate;
    private String owner;

    @JsonSerialize(using=ObjectIdSerializer.class)
    public ObjectId getId() {
        return _id;
    }

    public void setId(ObjectId id) {
        this._id = id;
    }
    public List<Long> getSampleIds() {
        return sampleIds;
    }

    public void setSampleIds(List<Long> sampleIds) {
        this.sampleIds = sampleIds;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}

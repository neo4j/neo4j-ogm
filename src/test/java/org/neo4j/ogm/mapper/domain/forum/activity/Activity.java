package org.neo4j.ogm.mapper.domain.forum.activity;

import org.neo4j.ogm.annotation.NodeId;

import java.util.Date;

public abstract class Activity {

    private Date date;
    private Long id;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @NodeId  // not strictly necessary, can always default to field id, but required to explicitly use this getter
    public Long getActivityId() {
        return id;
    }

    @NodeId
    public void setActivityId(Long id) {
        this.id = id;
    }
}

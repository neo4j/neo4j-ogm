package org.neo4j.ogm.mapper.domain.forum.activity;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

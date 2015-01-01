package org.neo4j.ogm.domain.convertible.date;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.neo4j.ogm.annotation.typeconversion.DateString;

import java.util.Date;

public class Memo {

    private Long id;
    private String memo;

    // uses default ISO 8601 date format
    private Date recorded;

    // declares a custom converter
    @Convert(DateNumericStringConverter.class)
    private Date approved;

    @DateString("yyyy-MM-dd")
    private Date actioned;

    @DateLong
    private Date closed;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Date getRecorded() {
        return recorded;
    }

    public void setRecorded(Date recorded) {
        this.recorded = recorded;
    }

    public Date getActioned() {
        return actioned;
    }

    @DateString("yyyy-MM-dd")
    public void setActioned(Date actioned) {
        this.actioned = actioned;
    }

    @DateLong
    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    public Date getApproved() {
        return approved;
    }

    @Convert(DateNumericStringConverter.class)
    public void setApproved(Date approved) {
        this.approved = approved;
    }

}

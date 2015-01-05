package org.neo4j.ogm.domain.satellites;

import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.util.Date;

public abstract class DomainObject {

    private Long id;
    private String ref;

    @DateLong
    private Date updated;

    public Long getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }


}

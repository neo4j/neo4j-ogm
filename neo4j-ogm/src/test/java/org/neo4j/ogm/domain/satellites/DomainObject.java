package org.neo4j.ogm.domain.satellites;

public abstract class DomainObject {

    private Long id;
    private String ref;

    public Long getId() {
        return id;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

}

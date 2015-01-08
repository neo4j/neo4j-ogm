package org.neo4j.ogm.domain.policy;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Person extends DomainObject {

    private Set<Policy> influenced = new HashSet<>();

    @Relationship(type="WRITES_POLICY")
    private Set<Policy> written = new HashSet<>();

    public Person(String name) {
        setName(name);
    }

    public Set<Policy> getInfluenced() {
        return influenced;
    }

    public void setInfluenced(Set<Policy> influenced) {
        this.influenced = influenced;
    }

    public Set<Policy> getWritten() {
        return written;
    }

    public void setWritten(Set<Policy> written) {
        this.written = written;
    }
}

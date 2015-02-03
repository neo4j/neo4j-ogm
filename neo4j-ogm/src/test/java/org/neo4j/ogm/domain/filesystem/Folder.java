package org.neo4j.ogm.domain.filesystem;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Folder {

    private Long id;
    private String name;
    private Set<Document> documents = new HashSet<>();

    @Relationship(type = "CONTAINS", direction= Relationship.OUTGOING)
    public Set<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Document> documents) {
        this.documents = documents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + name + '\'' +
                ", documents=" + documents.size() +
                '}';
    }
}

package org.neo4j.ogm.domain.filesystem;

import org.neo4j.ogm.annotation.Relationship;

public class Document {

    private Folder folder;
    private String name;
    private Long id;

    @Relationship(type = "CONTAINS", direction= Relationship.INCOMING)
    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
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
        return "Document{" +
                "folder=" + folder +
                ", name='" + name + '\'' +
                '}';
    }
}

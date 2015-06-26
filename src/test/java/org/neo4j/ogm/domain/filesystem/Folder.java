/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.domain.filesystem;

import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Vince Bickers
 */
public class Folder {

    private Long id;
    private String name;
    private Collection<Document> documents = new ArrayList<>();
    private Collection<Document> archived = new ArrayList<>();

    @Relationship(type = "CONTAINS", direction= Relationship.OUTGOING)
    public Collection<Document> getDocuments() {
        return documents;
    }

    @Relationship(type = "ARCHIVED", direction= Relationship.OUTGOING)
    public Collection<Document> getArchived() {
        return archived;
    }

    public void setDocuments(Collection<Document> documents) {
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
                ", archived=" + archived.size() +
                '}';
    }
}

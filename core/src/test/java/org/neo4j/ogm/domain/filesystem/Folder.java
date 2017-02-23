/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.domain.filesystem;

import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Vince Bickers
 */
public class Folder extends FileSystemEntity {

    @Relationship(type = "CONTAINS", direction = Relationship.OUTGOING)
    private Collection<Document> documents = new ArrayList<>();

    @Relationship(type = "ARCHIVED", direction = Relationship.OUTGOING)
    private Collection<Document> archived = new ArrayList<>();

    public Collection<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Collection<Document> documents) {
        this.documents = documents;
    }

    public Collection<Document> getArchived() {
        return archived;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "name='" + getName() + '\'' +
                ", documents=" + documents.size() +
                ", archived=" + archived.size() +
                '}';
    }
}

/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.filesystem;

import java.util.ArrayList;
import java.util.Collection;

import org.neo4j.ogm.annotation.Relationship;

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

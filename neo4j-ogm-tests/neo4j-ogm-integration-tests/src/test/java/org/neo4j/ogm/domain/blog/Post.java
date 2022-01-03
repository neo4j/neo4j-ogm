/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.domain.blog;

import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;

/**
 * @author Vince Bickers
 */
public class Post {

    private Long id;
    private String title;

    @Relationship(type = "NEXT", direction = Relationship.Direction.OUTGOING)
    private Post next;

    @Transient
    private Post previous;

    public Post() {
    }

    public Post(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Post getNext() {
        return next;
    }

    @PostLoad
    public void postLoad() {
        if (next != null) {
            next.previous = this;
        }
    }

    public void setNext(Post next) {
        this.next = next;
        if (next != null) {
            next.previous = this;
        }
    }

    public Post getPrevious() {
        return previous;
    }

    public void setPrevious(Post previous) {
        this.previous = previous;
    }

    public String toString() {
        return title + "(" + id + ")";
    }
}

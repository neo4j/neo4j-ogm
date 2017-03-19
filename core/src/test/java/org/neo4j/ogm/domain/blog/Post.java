/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

    @Relationship(type = "NEXT", direction = Relationship.OUTGOING)
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

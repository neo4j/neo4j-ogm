/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.domain.forum;

import java.util.List;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.activity.Post;

/**
 * @author Vince Bickers
 */
public class Topic {

    @Relationship(type = "HAS_POSTS")
    private List<Post> posts;
    private Boolean inActive;

    @Id @GeneratedValue
    private Long topicId;

    public Long getTopicId() {
        return topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
        setInActive(posts.isEmpty());
        this.posts = posts;
    }

    public Boolean getInActive() {
        return inActive;
    }

    public void setInActive(Boolean inActive) {
        this.inActive = inActive;
    }
}

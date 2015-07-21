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

package org.neo4j.ogm.domain.mappings;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Nils Dröge
 */
public class Article extends Entity
{
    private String title;

    @Relationship(type = "LIKE", direction = Relationship.INCOMING)
    private Set<Person> likes = new HashSet<Person>();

    public Article() {}

    public Set<Person> getLikes()
    {
        return likes;
    }

    public void setLikes(Set<Person> likes)
    {
        this.likes = likes;
    }

    @Override
    public String toString()
    {
        return "Article{" +
            "id:" + getNodeId() +
            ", title:'" + title + "'" +
            '}';
    }
}

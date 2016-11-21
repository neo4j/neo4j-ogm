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

package org.neo4j.ogm.domain.cineasts.partial;

import java.util.Date;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * @author Luanne Misquitta
 */
@RelationshipEntity(type = "KNOWS")
public class Knows {

    public Long id;

    @StartNode
    private Actor firstActor;

    @EndNode
    private Actor secondActor;

    private Date since;

    public Actor getFirstActor() {
        return firstActor;
    }

    public void setFirstActor(Actor firstActor) {
        this.firstActor = firstActor;
    }

    public Actor getSecondActor() {
        return secondActor;
    }

    public void setSecondActor(Actor secondActor) {
        this.secondActor = secondActor;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    @Override
    public String toString() {
        return "Knows {" +
                "since='" + since + '\'' +
                ", source='" + firstActor + '\'' +
                ", target='" + secondActor + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Knows knows = (Knows) o;

        if (!firstActor.equals(knows.firstActor)) return false;
        return secondActor.equals(knows.secondActor);

    }

    @Override
    public int hashCode() {
        int result = firstActor.hashCode();
        result = 31 * result + secondActor.hashCode();
        return result;
    }
}

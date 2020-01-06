/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Knows knows = (Knows) o;

        if (!firstActor.equals(knows.firstActor))
            return false;
        return secondActor.equals(knows.secondActor);
    }

    @Override
    public int hashCode() {
        int result = firstActor.hashCode();
        result = 31 * result + secondActor.hashCode();
        return result;
    }
}

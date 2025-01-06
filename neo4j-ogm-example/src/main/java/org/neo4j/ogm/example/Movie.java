/*
 * Copyright (c) 2002-2025 "Neo4j,"
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
package org.neo4j.ogm.example;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

@NodeEntity("Movie")
public class Movie {
    @Id
    String title;

    String tagline;

    @Relationship(type = "ACTED_IN", direction = Relationship.Direction.INCOMING)
    List<Actor> actors;

    @Relationship(type = "DIRECTED", direction = Relationship.Direction.INCOMING)
    List<Person> directors;

    @Relationship(type = "REVIEWED", direction = Relationship.Direction.INCOMING)
    List<Reviewer> reviewers;

    @Override public String toString() {
        return "Movie{" +
            "title='" + title + '\'' +
            ", actors=" + actors +
            ", directors=" + directors +
            ", reviewers=" + reviewers +
            '}';
    }

    public Movie withTagline(String newTagline) {
        Movie movie = new Movie();
        movie.title = this.title;
        movie.actors = this.actors;
        movie.directors = this.directors;
        movie.reviewers = reviewers;
        movie.tagline = newTagline;
        return movie;
    }
}

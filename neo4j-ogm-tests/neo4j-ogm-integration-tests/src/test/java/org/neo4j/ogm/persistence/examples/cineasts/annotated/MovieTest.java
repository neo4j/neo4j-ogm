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
package org.neo4j.ogm.persistence.examples.cineasts.annotated;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Michal Bachman
 * @author Mark Angrish
 */
public class MovieTest {

    @Test
    public void testDeserialiseMovie() {

        MetaData metadata = new MetaData("org.neo4j.ogm.domain.cineasts.annotated");
        Neo4jSession session = new Neo4jSession(metadata, true, new MoviesRequest());

        Movie movie = session.load(Movie.class, UUID.fromString("38ebe777-bc85-4810-8217-096f29a361f1"), 1);

        assertThat(movie.getTitle()).isEqualTo("Pulp Fiction");
        assertThat(movie.getRatings()).isNotNull();
        assertThat(movie.getRatings()).hasSize(1);

        Rating rating = movie.getRatings().iterator().next();

        assertThat(rating.getUser().getName()).isEqualTo("Michal");
        assertThat(rating.getMovie().getTitle()).isEqualTo("Pulp Fiction");
    }
}

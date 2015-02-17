/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.mapper.model.cineasts.annotated;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MovieTest {

    @Test
    public void testDeserialiseMovie() {

        MovieRequest movieRequest = new MovieRequest();

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
        Neo4jSession session = ((Neo4jSession) sessionFactory.openSession("dummy-url"));
        session.setRequest(movieRequest);

        Movie movie = session.load(Movie.class, 15L, 1);

        assertEquals("Pulp Fiction", movie.getTitle());
        assertNotNull(movie.getRatings());
        assertEquals(1, movie.getRatings().size());

        Rating rating = movie.getRatings().iterator().next();

        assertEquals("Michal", rating.getUser().getName());
        assertEquals("Pulp Fiction", rating.getMovie().getTitle());
    }
}

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

package org.neo4j.ogm.domain.cineasts.annotated;

import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

public class Movie {

    Long id;
    String title;
    int year;

    @Relationship(type="ACTS_IN", direction="INCOMING")
    Set<Role> cast;
    Set<Rating> ratings;

    Set<Nomination> nominations;


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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Set<Role> getCast() {
        return cast;
    }

    public void setCast(Set<Role> cast) {
        this.cast = cast;
    }

    public Set<Rating> getRatings() {
        return ratings;
    }

    @Relationship(type="RATED", direction=Relationship.INCOMING)
    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }

    public Set<Nomination> getNominations() {
        return nominations;
    }

    public void setNominations(Set<Nomination> nominations) {
        this.nominations = nominations;
    }
}

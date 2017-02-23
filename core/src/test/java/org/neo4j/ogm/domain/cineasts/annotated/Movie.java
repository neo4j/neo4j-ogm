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

package org.neo4j.ogm.domain.cineasts.annotated;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.UuidStringConverter;

/**
 * @author Michal Bachman
 * @author Vince Bickers
 * @author Mark Angrish
 */
@NodeEntity
public class Movie {

    Long id;

    @Convert(UuidStringConverter.class)
    @Index(unique = true, primary = true)
    private UUID uuid;

    @Index
    String title;

    int year;

    @Relationship(type = "ACTS_IN", direction = "INCOMING")
    Set<Role> roles = new HashSet<>();

    @Relationship(type = "RATED", direction = Relationship.INCOMING)
    Set<Rating> ratings;

    Set<Nomination> nominations;

    @Convert(URLConverter.class)
    URL imdbUrl;

    public Movie() {
    }

    public Movie(String title, int year) {
        this.uuid = UUID.randomUUID();
        this.title = title;
        this.year = year;
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Set<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }

    public Set<Nomination> getNominations() {
        return nominations;
    }

    public void setNominations(Set<Nomination> nominations) {
        this.nominations = nominations;
    }

    public URL getImdbUrl() {
        return imdbUrl;
    }

    public void setImdbUrl(URL imdbUrl) {
        this.imdbUrl = imdbUrl;
    }

    @Override
    public String toString() {
        return "Movie:" + title;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Movie movie = (Movie) o;

        if (title == null || movie.getTitle() == null) return false;

        if (!title.equals(movie.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }

    public UUID getUuid() {
        return uuid;
    }
}

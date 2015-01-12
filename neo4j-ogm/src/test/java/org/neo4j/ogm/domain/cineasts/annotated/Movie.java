package org.neo4j.ogm.domain.cineasts.annotated;

import java.util.Set;

public class Movie {

    Long id;
    String title;
    int year;
    Set<Role> cast;
    Set<Rating> ratings;

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

    public void setRatings(Set<Rating> ratings) {
        this.ratings = ratings;
    }
}

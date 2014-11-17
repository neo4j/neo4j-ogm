package org.neo4j.ogm.domain.cineasts;

import java.util.Set;

public class Movie {
    String id;
    String title;
    int year;
    Set<Role> cast;
}

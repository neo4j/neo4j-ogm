package org.neo4j.ogm.example;

/**
 * @author Gerrit Meier
 */
public class MovieNotFoundException extends RuntimeException {
    public MovieNotFoundException(String title) {
        super("Could not find movie with title %s".formatted(title));
    }
}

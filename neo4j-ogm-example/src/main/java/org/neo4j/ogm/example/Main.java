package org.neo4j.ogm.example;

/**
 * @author Gerrit Meier
 */
public class Main {

    public static void main(String[] args) {

        MovieService movieService = new MovieService();
        System.out.println(movieService.allMovies());
        System.out.println(movieService.updateTagline("The Matrix", "There is no spoon."));
        System.out.println(movieService.getRatings());
        System.out.println(movieService.findMovieByTitle("The Matrix2"));
    }
}

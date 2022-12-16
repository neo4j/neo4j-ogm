package org.neo4j.ogm.example;

import java.util.List;

/**
 * @author Gerrit Meier
 */
public record MovieRating(String title, float rating, List<String> reviewers) {
}

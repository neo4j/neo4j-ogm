package org.neo4j.cineasts.domain;

import org.neo4j.ogm.annotation.QueryResult;
import org.neo4j.ogm.annotation.ResultColumn;

@QueryResult
public interface MovieRecommendation {

    @ResultColumn("otherMovie")
    Movie getMovie();

    @ResultColumn("rating")
    int getRating();
}

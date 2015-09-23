package org.neo4j.ogm.driver.embedded;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.session.response.adapter.ResponseAdapter;
import org.neo4j.ogm.session.result.ResultProcessingException;

/**
 * @author vince
 */
public class RowStatisticsAdapter extends ModelAdapter implements ResponseAdapter<Result, String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String adapt(Result response) {
        try {
            String stats = mapper.writeValueAsString(response.getQueryStatistics());
            stats = stats.replace("Deleted", "_deleted");
            stats = stats.replace("Added", "_added");
            stats = stats.replace("Updates", "_updates");
            stats = stats.replace("Created", "_created");
            stats = stats.replace("Set", "_set");
            stats = stats.replace("Removed", "_removed");
            stats = stats.replace("deletedNodes", "nodes_deleted");
            stats = stats.replace("deletedRelationships", "relationships_deleted");

            return stats;

        } catch (Exception e) {
            throw new ResultProcessingException("Could not read response statistics", e);
        }
    }
}

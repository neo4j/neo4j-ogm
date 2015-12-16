package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.response.model.StatisticsModel;
import org.neo4j.ogm.result.ResultAdapter;

/**
 * @author vince
 */
public class StatisticsModelAdapter extends JsonAdapter implements ResultAdapter<Result, StatisticsModel> {


    @Override
    public StatisticsModel adapt(Result response) {
        try {
            org.neo4j.graphdb.QueryStatistics statistics = response.getQueryStatistics();
            String stats = mapper.writeValueAsString(statistics);
            stats = stats.replace("Deleted", "_deleted");
            stats = stats.replace("Added", "_added");
            stats = stats.replace("Updates", "_updates");
            stats = stats.replace("Created", "_created");
            stats = stats.replace("Set", "_set");
            stats = stats.replace("Removed", "_removed");
            stats = stats.replace("deletedNodes", "nodes_deleted");
            stats = stats.replace("deletedRelationships", "relationships_deleted");

            //Modify the string to include contains_updates as it is a calculated value
            String containsUpdates = ",\"contains_updates\":" + statistics.containsUpdates();
            int closingBraceIndex = stats.lastIndexOf("}");
            stats = stats.substring(0, closingBraceIndex) + containsUpdates + "}";

            return mapper.readValue(stats,StatisticsModel.class);

        } catch (Exception e) {
            throw new ResultProcessingException("Could not read response statistics", e);
        }
    }
}

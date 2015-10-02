package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.api.result.ResultAdapter;
import org.neo4j.ogm.driver.impl.model.StatisticsModel;
import org.neo4j.ogm.driver.impl.result.ResultProcessingException;

/**
 * @author vince
 */
public class StatisticsModelAdapter extends JsonAdapter implements ResultAdapter<Result, StatisticsModel> {


    @Override
    public StatisticsModel adapt(Result response) {
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

            return mapper.readValue(stats,StatisticsModel.class);

        } catch (Exception e) {
            throw new ResultProcessingException("Could not read response statistics", e);
        }
    }
}

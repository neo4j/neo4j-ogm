package org.neo4j.ogm.api.result;

import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.model.Query;
import org.neo4j.ogm.api.response.model.GraphRowModel;
import org.neo4j.ogm.api.response.model.GraphRowsModel;
import org.neo4j.ogm.api.response.model.RowModel;

/**
 *  The results of a query, modelled as a collection of GraphRow objects (both both graph and row data).
 *
 * @author Luanne Misquitta
 */
public class ResultGraphRowsModel implements Query<GraphRows> {

    GraphRowsModel model;

    public ResultGraphRowsModel() {
        model = new GraphRowsModel();
    }

    public GraphRows model() {
        return model;
    }

    public void addGraphRowResult(Graph graphModel, RowModel rowModel) {
        model.add(new GraphRowModel(graphModel, rowModel.getValues()));
    }

    public void addGraphRowResult(Graph graphModel, Object[] rowModel) {
        model.add(new GraphRowModel(graphModel, rowModel));
    }

}

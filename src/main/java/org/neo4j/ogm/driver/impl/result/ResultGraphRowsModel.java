package org.neo4j.ogm.driver.impl.result;

import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.result.DriverResult;
import org.neo4j.ogm.driver.impl.model.GraphRowModel;
import org.neo4j.ogm.driver.impl.model.GraphRowsModel;
import org.neo4j.ogm.driver.impl.model.RowModel;

/**
 *  The results of a query, modelled as a collection of GraphRow objects (both both graph and row data).
 *
 * @author Luanne Misquitta
 */
public class ResultGraphRowsModel implements DriverResult<GraphRows> {

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

package org.neo4j.ogm.result;

import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.Query;
import org.neo4j.ogm.response.model.DefaultGraphRowListModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;
import org.neo4j.ogm.response.model.DefaultRowModel;

/**
 *  The results of a query, modelled as a collection of GraphRow objects (both both graph and row data).
 *
 * @author Luanne Misquitta
 */
public class ResultGraphRowListModel implements Query<GraphRowListModel> {

    DefaultGraphRowListModel model;

    public ResultGraphRowListModel() {
        model = new DefaultGraphRowListModel();
    }

    public GraphRowListModel model() {
        return model;
    }

    public void addGraphRowResult(GraphModel graphModel, DefaultRowModel rowModel) {
        model.add(new DefaultGraphRowModel(graphModel, rowModel.getValues()));
    }

    public void addGraphRowResult(GraphModel graphModel, Object[] rowModel) {
        model.add(new DefaultGraphRowModel(graphModel, rowModel));
    }

}

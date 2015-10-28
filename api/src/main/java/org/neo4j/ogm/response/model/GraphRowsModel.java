package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.GraphRow;
import org.neo4j.ogm.model.GraphRows;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vince
 */
public class GraphRowsModel implements GraphRows {

    List<GraphRow> model = new ArrayList();

    @Override
    public List<GraphRow> model() {
        return model;
    }

    public void add(GraphRow graphRowModel) {
        model.add(graphRowModel);
    }
}

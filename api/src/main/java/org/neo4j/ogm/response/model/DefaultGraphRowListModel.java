package org.neo4j.ogm.response.model;

import org.neo4j.ogm.model.*;
import org.neo4j.ogm.model.GraphRowModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vince
 */
public class DefaultGraphRowListModel implements GraphRowListModel {

    List<GraphRowModel> model = new ArrayList();

    @Override
    public List<GraphRowModel> model() {
        return model;
    }

    public void add(GraphRowModel graphRowModel) {
        model.add(graphRowModel);
    }
}

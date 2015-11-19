package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.response.model.DefaultGraphRowModel;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultAdapter;

import java.util.*;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class GraphRowModelAdapter extends JsonAdapter implements ResultAdapter<Map<String, Object>, GraphRowModel> {

    private GraphModelAdapter graphModelAdapter = new GraphModelAdapter();

    private List<String> columns = new ArrayList<>();

    /**
     * Reads the next row from the result object and transforms it into a RowModel object
     *
     * @param data the data to transform, given as a map
     * @return @return the data transformed to an {@link RowModel}
     */
    public GraphRowModel adapt(Map<String, Object> data) {

        assert (columns != null);

        Set<Long> nodeIdentities = new HashSet();
        Set<Long> edgeIdentities = new HashSet();

        GraphModel graphModel = new DefaultGraphModel();
        List<String> variables = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        // there is no guarantee that the objects in the data are ordered the same way as required by the columns
        // so we use the columns information to extract them in the correct order for post-processing.
        Iterator<String> iterator = columns.iterator();

        adapt(iterator, data, graphModel, variables, values, nodeIdentities, edgeIdentities);

        DefaultRowModel rowModel = new DefaultRowModel(values.toArray(new Object[]{}), variables.toArray(new String[]{}));

        return new DefaultGraphRowModel(graphModel, rowModel.getValues());
  }

    private void adapt(Iterator<String> iterator, Map<String, Object> data, GraphModel graphModel, List<String> variables, List<Object> values, Set<Long> nodeIdentities, Set<Long> edgeIdentities) {

        while (iterator.hasNext()) {

            String key = iterator.next();
            variables.add(key);

            Object value = data.get(key);

            if (value.getClass().isArray()) {
                value = convertToIterable(value);
                Iterable collection = (Iterable) value;
                Iterator objects = collection.iterator();
                while (objects.hasNext()) {
                    Object element = objects.next();
                    adapt(element, graphModel, values, nodeIdentities, edgeIdentities);
                }
            }
            else {
                adapt(value, graphModel, values, nodeIdentities, edgeIdentities);
            }
        }

    }

    private void adapt(Object element, GraphModel graphModel, List<Object> values, Set<Long> nodeIdentities, Set<Long> edgeIdentities) {
        if (element instanceof Path) {
            graphModelAdapter.buildPath((Path) element, graphModel, nodeIdentities, edgeIdentities);
        }
        else if (element instanceof Node) {
            graphModelAdapter.buildNode((Node) element, graphModel, nodeIdentities);
        }
        else if (element instanceof Relationship) {
            graphModelAdapter.buildRelationship((Relationship) element, graphModel, edgeIdentities);
        }
        else {
            values.add(element);
        }
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}

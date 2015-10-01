package org.neo4j.ogm.driver.embedded.response;

import org.neo4j.ogm.driver.api.result.ResultAdapter;
import org.neo4j.ogm.driver.impl.model.RowModel;
import org.neo4j.ogm.driver.impl.result.ResultProcessingException;
import org.neo4j.ogm.driver.impl.result.ResultRowModel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This adapter will transform an embedded response into a json response
 *
 * @author vince
 */
public class RowModelAdapter extends JsonAdapter implements ResultAdapter<Map<String, Object>, RowModel> {


    private List<String> columns;

    /**
     * Reads the next row from the result object and transforms it into a JSON representation
     * compatible with the "row" type response from Neo's Http transactional end point.
     *
     * @param data the data to transform, given as a map
     * @return
     */
    public RowModel adapt(Map<String, Object> data) {

        StringBuilder sb = new StringBuilder();

        OPEN_OBJECT(sb);

        OPEN_ARRAY("row", sb);

        assert (columns != null);

        // there is no guarantee that the objects in the data are ordered the same way as required by the columns
        // so we use the columns information to extract them in the correct order for post-processing.
        Iterator<String> iterator = columns.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Object value = data.get(key);
            if (value instanceof Long) {
                buildIntegral((Long) value, sb);
            }
            else {
                throw new RuntimeException("Not handled: " + value.getClass());
            }
            if (iterator.hasNext()) {
                sb.append(COMMA);
            }
        }

        CLOSE_ARRAY(sb);
        CLOSE_OBJECT(sb);

        try {
            return new RowModel(mapper.readValue(sb.toString(), ResultRowModel.class).model());
        } catch (Exception e) {
            throw new ResultProcessingException("Could not parse result", e);
        }
    }

    private void buildIntegral(Long value, StringBuilder sb) {
        sb.append(value.toString());
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}

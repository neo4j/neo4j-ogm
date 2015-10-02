package org.neo4j.ogm.driver.bolt.response;

import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.ogm.driver.embedded.response.JsonAdapter;
import org.neo4j.ogm.driver.impl.result.ResultProcessingException;
import org.neo4j.ogm.driver.impl.result.ResultRowModel;
import org.neo4j.ogm.api.result.ResultAdapter;

import java.util.Iterator;

/**
 * @author vince
 */
public class RowModelAdapter extends JsonAdapter implements ResultAdapter<Result, ResultRowModel> {
    @Override
    public ResultRowModel adapt(Result response) {

        if (response.next()) {
            StringBuilder sb = new StringBuilder();

            OPEN_OBJECT(sb);

            OPEN_ARRAY("row", sb);

            Value value = response.get(0);

            Iterator<Value> iterator = value.iterator();
            while (iterator.hasNext()) {

                Value item = iterator.next();
                if (item.isInteger()) {
                    sb.append(item.javaLong());
                }
                else {
                    throw new RuntimeException("Not handled: " + value);
                }
                if (iterator.hasNext()) {
                    sb.append(COMMA);
                }
            }

            CLOSE_ARRAY(sb);

            CLOSE_OBJECT(sb);


            try {
                return mapper.readValue(sb.toString().getBytes(), ResultRowModel.class);
            } catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }
        return null;
    }
}

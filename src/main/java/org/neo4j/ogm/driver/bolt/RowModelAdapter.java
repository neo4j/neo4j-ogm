package org.neo4j.ogm.driver.bolt;

import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.ogm.driver.JsonAdapter;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.result.RowModelResult;
import org.neo4j.ogm.session.result.ResultAdapter;

import java.util.Iterator;

/**
 * @author vince
 */
public class RowModelAdapter extends JsonAdapter implements ResultAdapter<Result, RowModelResult> {
    @Override
    public RowModelResult adapt(Result response) {

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
                return mapper.readValue(sb.toString().getBytes(), RowModelResult.class);
            } catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }
        return null;
    }
}

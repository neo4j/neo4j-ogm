package org.neo4j.ogm.drivers.http.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.response.Response;
import org.neo4j.ogm.drivers.impl.json.JSONArray;
import org.neo4j.ogm.drivers.impl.json.JSONException;
import org.neo4j.ogm.drivers.impl.json.JSONObject;
import org.neo4j.ogm.drivers.impl.model.GraphModel;
import org.neo4j.ogm.drivers.impl.result.ResultGraphRowsModel;
import org.neo4j.ogm.drivers.impl.result.ResultProcessingException;
import org.neo4j.ogm.drivers.impl.json.JSONArray;
import org.neo4j.ogm.drivers.impl.model.GraphModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author vince
 */
public class GraphRowModelResponse extends AbstractHttpResponse implements Response<GraphRows> {

    protected static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphRowModelResponse.class);
    private static final String SCAN_TOKEN = "\"data";

    private final CloseableHttpResponse response;

    public GraphRowModelResponse(CloseableHttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.response = httpResponse;
    }

    @Override
    public GraphRows next() {

        String json = super.nextRecord();

        if (json != null) {
            try {
                ResultGraphRowsModel graphRowModelResult = new ResultGraphRowsModel();

                JSONObject jsonObject = getOuterObject(json);

                JSONArray dataObject = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataObject.length(); i++) {
                    String graphJson = dataObject.getJSONObject(i).getString("graph");
                    String rowJson = dataObject.getJSONObject(i).getString("row");
                    GraphModel graphModel = mapper.readValue(graphJson, GraphModel.class);
                    Object[] rows = mapper.readValue(rowJson, Object[].class);
                    graphRowModelResult.addGraphRowResult(graphModel, rows);
                }
                return graphRowModelResult.model();
            } catch (Exception e) {
                LOGGER.error("failed to parse: " + json);
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            response.close();
        } catch (IOException e) {
            throw new ResultProcessingException("Could not close response", e);
        }
    }

    @Override
    public String scanToken() {
        return SCAN_TOKEN;
    }

    protected JSONObject getOuterObject(String json) throws JSONException {
        JSONObject outerObject;
        try {
            outerObject = new JSONObject(json);
        } catch (JSONException e) {
            outerObject = new JSONObject(json + "]}"); //TODO enhance the JSONParser to not strip off these 2 characters
        }
        return outerObject;
    }
}

package org.neo4j.ogm.driver.http.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.driver.impl.json.JSONArray;
import org.neo4j.ogm.driver.impl.json.JSONException;
import org.neo4j.ogm.driver.impl.json.JSONObject;
import org.neo4j.ogm.driver.api.response.Response;
import org.neo4j.ogm.driver.impl.model.GraphModel;
import org.neo4j.ogm.driver.impl.model.GraphRowModel;
import org.neo4j.ogm.driver.impl.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author vince
 */
public class GraphRowModelResponse extends AbstractHttpResponse implements Response<GraphRowModel> {

    protected static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphRowModelResponse.class);
    private static final String SCAN_TOKEN = "\"data";

    private final CloseableHttpResponse response;

    public GraphRowModelResponse(CloseableHttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.response = httpResponse;
    }

    @Override
    public GraphRowModel next() {

        String json = super.nextRecord();

        if (json != null) {
            try {
                GraphRowModel graphRowModel = new GraphRowModel();

                JSONObject jsonObject = getOuterObject(json);

                JSONArray dataObject = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataObject.length(); i++) {
                    String graphJson = dataObject.getJSONObject(i).getString("graph");
                    String rowJson = dataObject.getJSONObject(i).getString("row");
                    GraphModel graphModel = mapper.readValue(graphJson, GraphModel.class);
                    Object[] rows = mapper.readValue(rowJson, Object[].class);
                    graphRowModel.addGraphRowResult(graphModel, rows);
                }
                return graphRowModel;
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

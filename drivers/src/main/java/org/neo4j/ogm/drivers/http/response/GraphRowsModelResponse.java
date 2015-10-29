package org.neo4j.ogm.drivers.http.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphModel;
import org.neo4j.ogm.result.ResultGraphRowListModel;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author vince
 */
public class GraphRowsModelResponse extends AbstractHttpResponse implements Response<GraphRowListModel> {

    protected static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(GraphRowsModelResponse.class);
    private static final String SCAN_TOKEN = "\"data";

    private final CloseableHttpResponse response;

    public GraphRowsModelResponse(CloseableHttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.response = httpResponse;
    }

    @Override
    public GraphRowListModel next() {

        String json = super.nextRecord();

        if (json != null) {
            try {
                ResultGraphRowListModel graphRowModelResult = new ResultGraphRowListModel();

                JSONObject jsonObject = getOuterObject(json);

                JSONArray dataObject = jsonObject.getJSONArray("data");

                for (int i = 0; i < dataObject.length(); i++) {
                    String graphJson = dataObject.getJSONObject(i).getString("graph");
                    String rowJson = dataObject.getJSONObject(i).getString("row");
                    DefaultGraphModel graphModel = mapper.readValue(graphJson, DefaultGraphModel.class);
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

package org.neo4j.ogm.driver.http.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.json.JSONArray;
import org.neo4j.ogm.json.JSONException;
import org.neo4j.ogm.json.JSONObject;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.model.StatisticsModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vince
 */
public class RowStatisticsModelResponse extends AbstractHttpResponse implements Response<RowStatisticsModel> {

    protected static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(RowStatisticsModelResponse.class);
    private static final String SCAN_TOKEN = "\"results";

    private final CloseableHttpResponse response;

    public RowStatisticsModelResponse(CloseableHttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.response = httpResponse;
    }

    @Override
    public RowStatisticsModel next() {

        String json = super.nextRecord();

        if (json != null) {
            try {
                RowStatisticsModel rowQueryStatisticsResult = new RowStatisticsModel();
                JSONObject jsonObject = getOuterObject(json);
                JSONArray columnsObject = jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("columns");
                columns = mapper.readValue(columnsObject.toString(), String[].class);
                JSONArray dataObject = jsonObject.getJSONArray("results").getJSONObject(0).getJSONArray("data");
                JSONObject statsJson = jsonObject.getJSONArray("results").getJSONObject(0).getJSONObject("stats");

                rowQueryStatisticsResult.setStats(mapper.readValue(statsJson.toString(),StatisticsModel.class));

                List<Object> rows = new ArrayList<>();
                for (int i = 0; i < dataObject.length(); i++) {
                    String rowJson = dataObject.getJSONObject(i).getString("row");
                    Object row = mapper.readValue(rowJson, Object.class);
                    rows.add(row);
                }
                rowQueryStatisticsResult.setRows(rows);
                return rowQueryStatisticsResult;
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

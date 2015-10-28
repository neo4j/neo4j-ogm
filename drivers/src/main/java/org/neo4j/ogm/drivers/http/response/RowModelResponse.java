package org.neo4j.ogm.drivers.http.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.api.model.Row;
import org.neo4j.ogm.api.response.Response;
import org.neo4j.ogm.api.response.model.RowModel;
import org.neo4j.ogm.api.result.ResultRowModel;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author vince
 */
public class RowModelResponse extends AbstractHttpResponse implements Response<Row> {

    protected static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(RowModelResponse.class);
    private static final String SCAN_TOKEN = "\"row";

    private final CloseableHttpResponse response;

    public RowModelResponse(CloseableHttpResponse httpResponse) throws IOException {
        super(httpResponse.getEntity().getContent());
        this.response = httpResponse;
    }

    @Override
    public Row next() {

        String json = super.nextRecord();

        if (json != null) {
            try {
                return new RowModel(mapper.readValue(json, ResultRowModel.class).model());
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
}

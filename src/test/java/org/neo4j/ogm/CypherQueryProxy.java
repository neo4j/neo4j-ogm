package org.neo4j.ogm;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.neo4j.ogm.mapper.cypher.CypherQuery;
import org.neo4j.ogm.mapper.cypher.Request;
import org.neo4j.ogm.mapper.cypher.ResponseStream;

import java.util.Collection;

public class CypherQueryProxy implements CypherQuery {

    private Request<GraphModel> request;

    public void setRequest(Request<GraphModel> request) {
        this.request = request;
    }

    @Override
    public ResponseStream<GraphModel> queryById(Long... ids) {
        return request.execute();
    }

    @Override
    public ResponseStream<GraphModel> queryByLabel(Collection<String> label) {
        return request.execute();
    }

    @Override
    public ResponseStream<GraphModel> queryByLabelAndId(Collection<String> label, Long... ids) {
        return request.execute();
    }

    @Override
    public ResponseStream<GraphModel> queryByProperty(Collection<String> labels, Property property) {
        return request.execute();
    }

    @Override
    public ResponseStream<GraphModel> queryByProperties(Collection<String> labels, Collection<Property> properties) {
        return request.execute();
    }
}

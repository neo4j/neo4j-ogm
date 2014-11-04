package org.neo4j.ogm;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.Property;
import org.neo4j.ogm.mapper.cypher.CypherQuery;
import org.neo4j.ogm.mapper.cypher.ResponseStream;

import java.util.Collection;

public class TestCypherQuery implements CypherQuery {

    private final ResponseStream<GraphModel> stream;

    public TestCypherQuery(String responseText, int recordsToRead) {
        this.stream = new TestResponseStream(recordsToRead, responseText);
    }

    @Override
    public ResponseStream<GraphModel> queryById(Long... ids) {
        return stream;
    }

    @Override
    public ResponseStream<GraphModel> queryByLabel(Collection<String> label) {
        return stream;
    }

    @Override
    public ResponseStream<GraphModel> queryByLabelAndId(Collection<String> label, Long... ids) {
        return stream;
    }

    @Override
    public ResponseStream<GraphModel> queryByProperty(Collection<String> labels, Property property) {
        return stream;
    }

    @Override
    public ResponseStream<GraphModel> queryByProperties(Collection<String> labels, Collection<Property> properties) {
        return stream;
    }
}

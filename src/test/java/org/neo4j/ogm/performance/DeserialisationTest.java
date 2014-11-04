package org.neo4j.ogm.performance;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.junit.Test;
import org.neo4j.ogm.mapper.GraphModelToObjectMapper;
import org.neo4j.ogm.mapper.ObjectGraphMapper;
import org.neo4j.ogm.mapper.cypher.ResponseStream;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.model.GraphBuilder;

import static org.junit.Assert.assertTrue;

public class DeserialisationTest {

    private static GraphModelToObjectMapper<GraphModel> instantiateMapper() {
        return new ObjectGraphMapper("org.neo4j.ogm.mapper.domain.bike");
    }
    @Test
    public void testAverageDeserialisationSpeed() throws Exception {

        int count = 1000;          // how many we're deserialising
        int target =3000;          // maximum permitted time (milliseconds) for that number;

        final DummyResponseStream responseStream = new DummyResponseStream(count);
        GraphModel graphModel = null;
        GraphModelToObjectMapper<GraphModel> mapper = instantiateMapper();
        long elapsed = -System.currentTimeMillis();

        while ((graphModel = responseStream.next()) != null) {
            mapper.load(Bike.class,graphModel);
        }

        elapsed += System.currentTimeMillis();

        System.out.println("Deserialised Bike " + count + " times in " + elapsed + " milliseconds");
        assertTrue(elapsed < target);
    }

    static class DummyResponseStream implements ResponseStream<GraphModel> {

        private int count = 0;

        public DummyResponseStream(int records) {
            this.count = records;
        }

        public GraphModel next() {
            if (hasNext()) {
                count--;
                String json = nextResponse();
                try {
                    return GraphBuilder.build(json);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        public boolean hasNext() {
            return count > 0;
        }

        private String nextResponse() {
            return cypher();
        }

    }

    private static String cypher() {
        return
                "{\"graph\": { " +
                        "\"nodes\" :[ " +
                        "{\"id\" : \"15\",\"labels\" : [ \"Bike\"], \"properties\" : { \"colours\" :[\"red\", \"black\"] } }, " +
                        "{\"id\" : \"16\",\"labels\" : [ \"Wheel\", \"FrontWheel\" ],\"properties\" : {\"spokes\" : 3 } }, " +
                        "{\"id\" : \"17\",\"labels\" : [ \"Wheel\", \"BackWheel\" ],\"properties\" : {\"spokes\" : 5 } }, " +
                        "{\"id\" : \"18\",\"labels\" : [ \"Frame\" ],\"properties\" : {\"size\" : 27 } }, " +
                        "{\"id\" : \"19\",\"labels\" : [ \"Saddle\" ],\"properties\" : {\"price\" : 42.99, \"material\" : \"plastic\" } } " +
                        "], " +
                        "\"relationships\": [" +
                        "{\"id\":\"141\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"16\",\"properties\":{ \"purchased\" : 20130917 }}, " +
                        "{\"id\":\"142\",\"type\":\"HAS_WHEEL\",\"startNode\":\"15\",\"endNode\":\"17\",\"properties\":{ \"purchased\" : 20130917 }}," +
                        "{\"id\":\"143\",\"type\":\"HAS_FRAME\",\"startNode\":\"15\",\"endNode\":\"18\",\"properties\":{ \"purchased\" : 20130917 }}," +
                        "{\"id\":\"144\",\"type\":\"HAS_SADDLE\",\"startNode\":\"15\",\"endNode\":\"19\",\"properties\":{\"purchased\" : 20130922 }} " +
                        "] " +
                        "} }";
    }


}
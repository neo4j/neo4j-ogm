package org.neo4j.ogm.mapper.model;

import org.graphaware.graphmodel.neo4j.GraphModel;

public class CollectionModel {

    private DummyResponseStream responseStream = new DummyResponseStream(2);

    private static String[] jsonModel = {

        "{\"graph\": { " +
            "\"nodes\" :[ " +
                "{\"id\" : \"1\",\"labels\" : [ \"Parent\"], \"properties\" : { \"name\" :\"James\" } }, " +
                "{\"id\" : \"2\",\"labels\" : [ \"Child\" ],\"properties\" : {\"name\" : \"Bill\" } }" +
            "], " +
            "\"relationships\": [" +
                "{\"id\":\"100\",\"type\":\"HAS_CHILD\",\"startNode\":\"1\",\"endNode\":\"2\",\"properties\":{}}" +
            "] " +
        "} }"
        ,

        "{\"graph\": { " +
            "\"nodes\" :[ " +
                "{\"id\" : \"1\",\"labels\" : [ \"Parent\"], \"properties\" : { \"name\" :\"James\" } }, " +
                "{\"id\" : \"3\",\"labels\" : [ \"Child\" ],\"properties\" : {\"name\" : \"Mary\" } }" +
                "], " +
            "\"relationships\": [" +
                "{\"id\":\"101\",\"type\":\"HAS_CHILD\",\"startNode\":\"1\",\"endNode\":\"3\",\"properties\":{}}" +
                "] " +
        "} }"
    };


    public class DummyResponseStream {

        private int records = 0;
        private int count = 0;

        public DummyResponseStream(int records) {
            this.records = records;
        }

        public GraphModel next() throws Exception {
            if (hasNext()) {
                String json = jsonModel[count];
                //System.out.println("response: " + json);
                count++;
                return GraphBuilder.build(json);
            }
            return null;
        }

        private boolean hasNext() {
            return count < records;
        }

    }

    public DummyResponseStream getResponseStream() {
        return responseStream;
    }
}

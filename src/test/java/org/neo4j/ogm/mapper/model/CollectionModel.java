package org.neo4j.ogm.mapper.model;

public class CollectionModel extends DummyRequest {

    public CollectionModel() {
        setResponse(jsonModel);
    }

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


}

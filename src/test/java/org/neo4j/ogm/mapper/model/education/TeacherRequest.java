package org.neo4j.ogm.mapper.model.education;

import org.neo4j.ogm.mapper.model.DummyRequest;

/**
 * MATCH p=(t:TEACHER)--(c) return p
 */
public class TeacherRequest extends DummyRequest {

    public TeacherRequest() {
        setResponse(jsonModel);
    }

    private static String[] jsonModel = {

            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"20\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mr Thomas\" } }, " +
                    "{\"id\" : \"2\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"English\" } }," +
                    "{\"id\" : \"3\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Maths\" } }," +
                    "{\"id\" : \"4\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Physics\" } }" +
                    "], " +
                "\"relationships\": [" +
                "{\"id\":\"202\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"2\",\"properties\":{}}," +
                "{\"id\":\"203\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"3\",\"properties\":{}}," +
                "{\"id\":\"204\",\"type\":\"TEACHES\",\"startNode\":\"20\",\"endNode\":\"4\",\"properties\":{}} " +
                "] " +
            "} }"

            ,

            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"21\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Mrs Roberts\" } }, " +
                    "{\"id\" : \"6\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"PE\" } }," +
                    "{\"id\" : \"2\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"English\" } }," +
                    "{\"id\" : \"7\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"History\" } } " +
                    "], " +
                "\"relationships\": [" +
                    "{\"id\":\"212\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"2\",\"properties\":{}}," +
                    "{\"id\":\"216\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"6\",\"properties\":{}}," +
                    "{\"id\":\"217\",\"type\":\"TEACHES\",\"startNode\":\"21\",\"endNode\":\"7\",\"properties\":{}} " +
                    "] " +
            "} }"

            ,

            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"22\",\"labels\" : [ \"Teacher\"], \"properties\" : { \"name\" :\"Miss Young\" } }, " +
                    "{\"id\" : \"5\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Philosophy and Ethics\" } }," +
                    "{\"id\" : \"7\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"History\" } }," +
                    "{\"id\" : \"8\",\"labels\" : [ \"Course\" ],\"properties\" : {\"name\" : \"Geography\" } }" +
                    "], " +
                "\"relationships\": [" +
                    "{\"id\":\"225\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"5\",\"properties\":{}}," +
                    "{\"id\":\"226\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"7\",\"properties\":{}}," +
                    "{\"id\":\"228\",\"type\":\"TEACHES\",\"startNode\":\"22\",\"endNode\":\"8\",\"properties\":{}}" +
                    "] " +
            "} }"

    };


}

package org.neo4j.ogm.mapper.model.education;


import org.neo4j.ogm.mapper.model.DummyRequest;

/**
 * MATCH p=(c:CLASS)--(s) {WHERE id(c) in (n, m, ...z)} RETURN p
 */
public class CourseRequestMrThomas extends DummyRequest {

    public CourseRequestMrThomas() {
        setResponse(jsonModel);
    }
    // Mr Thomas teaches English, Maths and Physics

    private static String[] jsonModel = {
            // English set : all
            "{\"graph\": { " +
                "\"nodes\" :[ " +
                    "{\"id\" : \"2\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"English\" } }, " +

                    "{\"id\" : \"101\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Alex\" } }," +
                    "{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
                    "{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
                    "{\"id\" : \"104\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Daisy\" } }," +
                    "{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
                    "{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
                    "{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
                    "{\"id\" : \"108\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Hannah\" } }," +
                    "{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
                    "{\"id\" : \"110\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Jasmin\" } }," +
                    "{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
                    "{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
                    "{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
                    "{\"id\" : \"114\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Neil\" } }," +
                    "{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
                    "{\"id\" : \"116\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Peter\" } }," +
                    "{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
                    "{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
                    "{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
                    "{\"id\" : \"120\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Tom\" } }," +
                    "{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
                    "{\"id\" : \"122\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Veronica\" } }," +
                    "{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }," +
                    "{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }," +
                    "{\"id\" : \"125\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Yvette\" } }," +
                    "{\"id\" : \"126\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Zack\" } }" +

                "], " +
                "\"relationships\": [" +
                    "{\"id\":\"2101\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"101\",\"properties\":{}}," +
                    "{\"id\":\"2102\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"102\",\"properties\":{}}," +
                    "{\"id\":\"2103\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"103\",\"properties\":{}}," +
                    "{\"id\":\"2104\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"104\",\"properties\":{}}," +
                    "{\"id\":\"2105\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"105\",\"properties\":{}}," +
                    "{\"id\":\"2106\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"106\",\"properties\":{}}," +
                    "{\"id\":\"2107\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"107\",\"properties\":{}}," +
                    "{\"id\":\"2108\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"108\",\"properties\":{}}," +
                    "{\"id\":\"2109\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"109\",\"properties\":{}}," +
                    "{\"id\":\"2110\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"110\",\"properties\":{}}," +
                    "{\"id\":\"2111\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"111\",\"properties\":{}}," +
                    "{\"id\":\"2112\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"112\",\"properties\":{}}," +
                    "{\"id\":\"2113\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"113\",\"properties\":{}}," +
                    "{\"id\":\"2114\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"114\",\"properties\":{}}," +
                    "{\"id\":\"2115\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"115\",\"properties\":{}}," +
                    "{\"id\":\"2116\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"116\",\"properties\":{}}," +
                    "{\"id\":\"2117\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"117\",\"properties\":{}}," +
                    "{\"id\":\"2118\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"118\",\"properties\":{}}," +
                    "{\"id\":\"2119\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"119\",\"properties\":{}}," +
                    "{\"id\":\"2120\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"120\",\"properties\":{}}," +
                    "{\"id\":\"2121\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"121\",\"properties\":{}}," +
                    "{\"id\":\"2122\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"122\",\"properties\":{}}," +
                    "{\"id\":\"2123\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"123\",\"properties\":{}}," +
                    "{\"id\":\"2124\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"124\",\"properties\":{}}," +
                    "{\"id\":\"2125\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"125\",\"properties\":{}}," +
                    "{\"id\":\"2126\",\"type\":\"ENROLLED\",\"startNode\":\"2\",\"endNode\":\"126\",\"properties\":{}} " +
                "] " +
            "} }"
            ,
            // Maths set : all
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"3\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Mathematics\" } }, " +

                    "{\"id\" : \"101\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Alex\" } }," +
                    "{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
                    "{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
                    "{\"id\" : \"104\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Daisy\" } }," +
                    "{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
                    "{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
                    "{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
                    "{\"id\" : \"108\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Hannah\" } }," +
                    "{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
                    "{\"id\" : \"110\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Jasmin\" } }," +
                    "{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
                    "{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
                    "{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
                    "{\"id\" : \"114\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Neil\" } }," +
                    "{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
                    "{\"id\" : \"116\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Peter\" } }," +
                    "{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
                    "{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
                    "{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
                    "{\"id\" : \"120\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Tom\" } }," +
                    "{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
                    "{\"id\" : \"122\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Veronica\" } }," +
                    "{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }," +
                    "{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }," +
                    "{\"id\" : \"125\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Yvette\" } }," +
                    "{\"id\" : \"126\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Zack\" } }" +

                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"3101\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"101\",\"properties\":{}}," +
                    "{\"id\":\"3102\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"102\",\"properties\":{}}," +
                    "{\"id\":\"3103\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"103\",\"properties\":{}}," +
                    "{\"id\":\"3104\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"104\",\"properties\":{}}," +
                    "{\"id\":\"3105\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"105\",\"properties\":{}}," +
                    "{\"id\":\"3106\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"106\",\"properties\":{}}," +
                    "{\"id\":\"3107\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"107\",\"properties\":{}}," +
                    "{\"id\":\"3108\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"108\",\"properties\":{}}," +
                    "{\"id\":\"3109\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"109\",\"properties\":{}}," +
                    "{\"id\":\"3110\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"110\",\"properties\":{}}," +
                    "{\"id\":\"3111\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"111\",\"properties\":{}}," +
                    "{\"id\":\"3112\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"112\",\"properties\":{}}," +
                    "{\"id\":\"3113\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"113\",\"properties\":{}}," +
                    "{\"id\":\"3114\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"114\",\"properties\":{}}," +
                    "{\"id\":\"3115\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"115\",\"properties\":{}}," +
                    "{\"id\":\"3116\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"116\",\"properties\":{}}," +
                    "{\"id\":\"3117\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"117\",\"properties\":{}}," +
                    "{\"id\":\"3118\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"118\",\"properties\":{}}," +
                    "{\"id\":\"3119\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"119\",\"properties\":{}}," +
                    "{\"id\":\"3120\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"120\",\"properties\":{}}," +
                    "{\"id\":\"3121\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"121\",\"properties\":{}}," +
                    "{\"id\":\"3122\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"122\",\"properties\":{}}," +
                    "{\"id\":\"3123\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"123\",\"properties\":{}}," +
                    "{\"id\":\"3124\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"124\",\"properties\":{}}," +
                    "{\"id\":\"3125\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"125\",\"properties\":{}}," +
                    "{\"id\":\"3126\",\"type\":\"ENROLLED\",\"startNode\":\"3\",\"endNode\":\"126\",\"properties\":{}} " +
                    "] " +
                    "} }"
            ,
            // Physics set : odd(id)
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"4\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Physics\" } }, " +

                    "{\"id\" : \"101\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Alex\" } }," +
                    "{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
                    "{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
                    "{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
                    "{\"id\" : \"109\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ignacio\" } }," +
                    "{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
                    "{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
                    "{\"id\" : \"115\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Otto\" } }," +
                    "{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
                    "{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
                    "{\"id\" : \"121\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Ulf\" } }," +
                    "{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }," +
                    "{\"id\" : \"125\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Yvette\" } }" +

                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"4101\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"101\",\"properties\":{}}," +
                    "{\"id\":\"4103\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"103\",\"properties\":{}}," +
                    "{\"id\":\"4106\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"105\",\"properties\":{}}," +
                    "{\"id\":\"4107\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"107\",\"properties\":{}}," +
                    "{\"id\":\"4109\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"109\",\"properties\":{}}," +
                    "{\"id\":\"4111\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"111\",\"properties\":{}}," +
                    "{\"id\":\"4113\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"113\",\"properties\":{}}," +
                    "{\"id\":\"4115\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"115\",\"properties\":{}}," +
                    "{\"id\":\"4117\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"117\",\"properties\":{}}," +
                    "{\"id\":\"4119\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"119\",\"properties\":{}}," +
                    "{\"id\":\"4121\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"121\",\"properties\":{}}," +
                    "{\"id\":\"4123\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"123\",\"properties\":{}}," +
                    "{\"id\":\"4125\",\"type\":\"ENROLLED\",\"startNode\":\"4\",\"endNode\":\"125\",\"properties\":{}}" +
                    "] " +
            "} }"
    };


}

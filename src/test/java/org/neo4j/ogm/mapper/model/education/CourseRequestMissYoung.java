package org.neo4j.ogm.mapper.model.education;


import org.neo4j.ogm.mapper.model.DummyRequest;

/**
 * MATCH p=(c:CLASS)--(s) {WHERE id(c) in (n, m, ...z)} RETURN p
 */
public class CourseRequestMissYoung extends DummyRequest {

    public CourseRequestMissYoung() {
        setResponse(jsonModel);
    }

    // Miss Young teaches Philosophy & Ethics, History and Geography
    private static String[] jsonModel = {
            // Philosophy and Ethics set: all
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"5\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Philosophy and Ethics\" } }, " +

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
                    "{\"id\":\"5101\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"101\",\"properties\":{}}," +
                    "{\"id\":\"5102\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"102\",\"properties\":{}}," +
                    "{\"id\":\"5103\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"103\",\"properties\":{}}," +
                    "{\"id\":\"5104\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"104\",\"properties\":{}}," +
                    "{\"id\":\"5106\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"105\",\"properties\":{}}," +
                    "{\"id\":\"5106\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"106\",\"properties\":{}}," +
                    "{\"id\":\"5107\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"107\",\"properties\":{}}," +
                    "{\"id\":\"5108\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"108\",\"properties\":{}}," +
                    "{\"id\":\"5109\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"109\",\"properties\":{}}," +
                    "{\"id\":\"5110\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"110\",\"properties\":{}}," +
                    "{\"id\":\"5111\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"111\",\"properties\":{}}," +
                    "{\"id\":\"5112\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"112\",\"properties\":{}}," +
                    "{\"id\":\"5113\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"113\",\"properties\":{}}," +
                    "{\"id\":\"5114\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"114\",\"properties\":{}}," +
                    "{\"id\":\"5115\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"115\",\"properties\":{}}," +
                    "{\"id\":\"5116\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"116\",\"properties\":{}}," +
                    "{\"id\":\"5117\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"117\",\"properties\":{}}," +
                    "{\"id\":\"5118\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"118\",\"properties\":{}}," +
                    "{\"id\":\"5119\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"119\",\"properties\":{}}," +
                    "{\"id\":\"5120\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"120\",\"properties\":{}}," +
                    "{\"id\":\"5121\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"121\",\"properties\":{}}," +
                    "{\"id\":\"5122\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"121\",\"properties\":{}}," +
                    "{\"id\":\"5123\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"123\",\"properties\":{}}," +
                    "{\"id\":\"5124\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"124\",\"properties\":{}}," +
                    "{\"id\":\"5125\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"125\",\"properties\":{}}," +
                    "{\"id\":\"5126\",\"type\":\"ENROLLED\",\"startNode\":\"5\",\"endNode\":\"126\",\"properties\":{}} " +
                    "] " +
            "} }"
            ,
            // History set even(id)
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"7\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"History\" } }, " +

                    "{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
                    "{\"id\" : \"104\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Daisy\" } }," +
                    "{\"id\" : \"106\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Frankie\" } }," +
                    "{\"id\" : \"108\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Hannah\" } }," +
                    "{\"id\" : \"110\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Jasmin\" } }," +
                    "{\"id\" : \"112\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Lyra\" } }," +
                    "{\"id\" : \"114\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Neil\" } }," +
                    "{\"id\" : \"116\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Peter\" } }," +
                    "{\"id\" : \"118\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Rachel\" } }," +
                    "{\"id\" : \"120\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Tom\" } }," +
                    "{\"id\" : \"122\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Veronica\" } }," +
                    "{\"id\" : \"124\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Xavier\" } }," +
                    "{\"id\" : \"126\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Zack\" } }" +

                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"7102\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"102\",\"properties\":{}}," +
                    "{\"id\":\"7104\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"104\",\"properties\":{}}," +
                    "{\"id\":\"7106\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"106\",\"properties\":{}}," +
                    "{\"id\":\"7108\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"108\",\"properties\":{}}," +
                    "{\"id\":\"7110\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"110\",\"properties\":{}}," +
                    "{\"id\":\"7112\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"112\",\"properties\":{}}," +
                    "{\"id\":\"7114\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"114\",\"properties\":{}}," +
                    "{\"id\":\"7116\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"116\",\"properties\":{}}," +
                    "{\"id\":\"7118\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"118\",\"properties\":{}}," +
                    "{\"id\":\"7120\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"120\",\"properties\":{}}," +
                    "{\"id\":\"7122\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"122\",\"properties\":{}}," +
                    "{\"id\":\"7124\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"124\",\"properties\":{}}," +
                    "{\"id\":\"7126\",\"type\":\"ENROLLED\",\"startNode\":\"7\",\"endNode\":\"126\",\"properties\":{}} " +
                    "] " +
            "} }"
            ,
            // Geography set : isPrime(id modulo 100)
            "{\"graph\": { " +
                    "\"nodes\" :[ " +
                    "{\"id\" : \"8\",\"labels\" : [ \"Course\"], \"properties\" : { \"name\" :\"Geography\" } }, " +

                    "{\"id\" : \"102\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Barry\" } }," +
                    "{\"id\" : \"103\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Carmen\" } }," +
                    "{\"id\" : \"105\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Eloise\" } }," +
                    "{\"id\" : \"107\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Gavin\" } }," +
                    "{\"id\" : \"111\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Kent\" } }," +
                    "{\"id\" : \"113\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Maria\" } }," +
                    "{\"id\" : \"117\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Quentin\" } }," +
                    "{\"id\" : \"119\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Suzanne\" } }," +
                    "{\"id\" : \"123\",\"labels\" : [ \"Student\" ],\"properties\" : {\"name\" : \"Will\" } }" +

                    "], " +
                    "\"relationships\": [" +
                    "{\"id\":\"8102\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"102\",\"properties\":{}}," +
                    "{\"id\":\"8103\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"103\",\"properties\":{}}," +
                    "{\"id\":\"8105\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"105\",\"properties\":{}}," +
                    "{\"id\":\"8107\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"107\",\"properties\":{}}," +
                    "{\"id\":\"8109\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"109\",\"properties\":{}}," +
                    "{\"id\":\"8111\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"111\",\"properties\":{}}," +
                    "{\"id\":\"8113\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"113\",\"properties\":{}}," +
                    "{\"id\":\"8117\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"117\",\"properties\":{}}," +
                    "{\"id\":\"8119\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"119\",\"properties\":{}}," +
                    "{\"id\":\"8123\",\"type\":\"ENROLLED\",\"startNode\":\"8\",\"endNode\":\"123\",\"properties\":{}}" +
                    "] " +
            "} }"

    };


}

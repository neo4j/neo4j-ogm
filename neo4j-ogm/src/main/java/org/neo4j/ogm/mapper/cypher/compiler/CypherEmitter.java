package org.neo4j.ogm.mapper.cypher.compiler;

import java.util.Map;
import java.util.Set;

public interface CypherEmitter {

    /**
     * Emits one or more Cypher clauses.
     *
     * @param queryBuilder The {@code StringBuilder} to which the Cypher should be appended
     * @param parameters A {@link Map} to which Cypher parameter values may optionally be added as the query is built up
     * @param varStack The variable stack carried through the query, to which this emitter's variable name may be added
     */

    boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack);
}

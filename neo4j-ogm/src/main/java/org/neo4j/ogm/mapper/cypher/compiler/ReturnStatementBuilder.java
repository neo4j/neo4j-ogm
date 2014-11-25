package org.neo4j.ogm.mapper.cypher.compiler;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ReturnStatementBuilder implements CypherEmitter {
    @Override
    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        if (!varStack.isEmpty()) {
            queryBuilder.append(" RETURN ");
            for (Iterator<String> it = varStack.iterator(); it.hasNext(); ) {
                String var = it.next();
                queryBuilder.append("id(");
                queryBuilder.append(var);
                queryBuilder.append(") AS ");
                queryBuilder.append(var);
                if (it.hasNext()) {
                    queryBuilder.append(", ");
                }
            }
        }
        return !varStack.isEmpty();
    }
}

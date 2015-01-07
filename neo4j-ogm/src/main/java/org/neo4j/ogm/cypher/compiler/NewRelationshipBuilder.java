package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Set;

public class NewRelationshipBuilder implements CypherEmitter {

    private final String type;
    private final String src;
    private final String tgt;

    public NewRelationshipBuilder(String type, String src, String tgt) {
        this.type = type;
        this.src = src;
        this.tgt = tgt;
    }

    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(NodeBuilder.toCsv(varStack));
        }

        if (!varStack.contains(src)) {
            queryBuilder.append(" MATCH (");
            queryBuilder.append(src);
            queryBuilder.append(") WHERE id(");
            queryBuilder.append(src);
            queryBuilder.append(")=");
            queryBuilder.append(src.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(src);
        }

        if (!varStack.contains(tgt)) {
            queryBuilder.append(" MATCH (");
            queryBuilder.append(tgt);
            queryBuilder.append(") WHERE id(");
            queryBuilder.append(tgt);
            queryBuilder.append(")=");
            queryBuilder.append(tgt.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(tgt);
        }

        queryBuilder.append(" MERGE (");
        queryBuilder.append(src);
        queryBuilder.append(")-[:");
        queryBuilder.append(type);
        queryBuilder.append("]->(");
        queryBuilder.append(tgt);
        queryBuilder.append(")");

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewRelationshipBuilder that = (NewRelationshipBuilder) o;

        if (!src.equals(that.src)) return false;
        if (!tgt.equals(that.tgt)) return false;
        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + src.hashCode();
        result = 31 * result + tgt.hashCode();
        return result;
    }
}

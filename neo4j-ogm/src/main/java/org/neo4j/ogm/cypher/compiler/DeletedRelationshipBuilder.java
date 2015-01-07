package org.neo4j.ogm.cypher.compiler;

import java.util.Map;
import java.util.Set;

public class DeletedRelationshipBuilder implements CypherEmitter {

    private final String type;
    private final String src;
    private final String tgt;
    private final String rid;

    public DeletedRelationshipBuilder(String type, String src, String tgt, String rid) {
        this.type = type;
        this.src = src;
        this.tgt = tgt;
        this.rid = rid;
    }

    public boolean emit(StringBuilder queryBuilder, Map<String, Object> parameters, Set<String> varStack) {

        if (!varStack.isEmpty()) {
            queryBuilder.append(" WITH ").append(NodeBuilder.toCsv(varStack));
        }

        queryBuilder.append(" MATCH (");
        queryBuilder.append(src);
        queryBuilder.append(")-[");
        queryBuilder.append(rid);
        queryBuilder.append(":");
        queryBuilder.append(type);
        queryBuilder.append("]->(");
        queryBuilder.append(tgt);
        queryBuilder.append(")");

        boolean where = false;

        if (!varStack.contains(src)) {
            queryBuilder.append(" WHERE id(");
            queryBuilder.append(src);
            queryBuilder.append(")=");
            queryBuilder.append(src.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(src);
            where = true;
        }

        if (!varStack.contains(tgt)) {
            if (where) {
                queryBuilder.append(" AND id(");
            } else {
                queryBuilder.append(" WHERE id(");
            }
            queryBuilder.append(tgt);
            queryBuilder.append(")=");
            queryBuilder.append(tgt.substring(1)); // existing nodes have an id. we pass it in as $id
            varStack.add(tgt);
        }

        queryBuilder.append(" DELETE ");
        queryBuilder.append(rid);

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeletedRelationshipBuilder that = (DeletedRelationshipBuilder) o;

        if (!rid.equals(that.rid)) return false;
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
        result = 31 * result + rid.hashCode();
        return result;
    }
}

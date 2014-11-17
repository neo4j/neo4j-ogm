package org.neo4j.ogm.unit.mapper.cypher;

import java.util.ArrayList;
import java.util.List;

public class CypherStatements {

    private static final String PROLOGUE = "{\"statements\" : [";
    private static final String EPILOGUE = " ] }";

    private List<String> statements = new ArrayList<>();

    public CypherStatements add(String statement) {
        statements.add(statement);
        return this;
    }

    private String statements() {
        if (!statements.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String statement : statements) {
                sb.append(", { \"statement\": \"");
                sb.append(statement);

                // hack for now
                if (statement.startsWith("MATCH")) {
                    sb.append("\", \"resultDataContents\" : [ \"graph\" ] }");
                }
                else {
                    sb.append("\" }");
                }

            }

            return sb.toString().substring(1);
        } else return "";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(PROLOGUE);
        sb.append(statements());
        sb.append(EPILOGUE);
        return sb.toString();
    }

}

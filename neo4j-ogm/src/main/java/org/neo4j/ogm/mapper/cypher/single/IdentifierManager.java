package org.neo4j.ogm.mapper.cypher.single;

/**
 * Manages identifiers used within the scope of a single Cypher query.
 */
public class IdentifierManager {

    private static final String VAR_FORMAT = "_%d";

    private int idCounter;

    /**
     * Generates the next variable name to use in the context of a Cypher query.
     *
     * @return The next variable name to use, never <code>null</code>
     */
    public String nextIdentifier() {
        return String.format(VAR_FORMAT, this.idCounter++);
    }

}

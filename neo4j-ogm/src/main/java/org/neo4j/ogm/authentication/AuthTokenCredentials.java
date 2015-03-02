package org.neo4j.ogm.authentication;

public class AuthTokenCredentials implements Neo4jCredentials<String> {

    private final String token;

    public AuthTokenCredentials(String token) {
        this.token = token;
    }

    @Override
    public String credentials() {
        return token;
    }
}

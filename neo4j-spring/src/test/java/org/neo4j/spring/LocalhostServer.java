package org.neo4j.spring;

public class LocalhostServer implements Neo4jServer {

    public String url() {
        return "http://localhost:7474";
    }

}
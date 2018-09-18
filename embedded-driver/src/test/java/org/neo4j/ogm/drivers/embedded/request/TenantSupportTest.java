package org.neo4j.ogm.drivers.embedded.request;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;

public class TenantSupportTest {


    @Test
    public void testCustomerSet() throws IOException {
        Path logPath = Paths.get("/Users/gerritmeier/Downloads/logs/uniqueQueries2.txt");
        List<String> lines = Files.readAllLines(logPath);

            TenantSupport tenantSupport = new TenantSupport("Blubb");
        for (String line : lines) {
            System.out.println("+++++++++++++++++++++++");
            // MATCH (c:IdEntity:ConfigEntity {objectState:'COMMITTED'}) WHERE ((c)-[:PREDECESSOR]->({objectState:'DEPLOYING'}))
            // MATCH ( {objectState: "COMMITTED"}) WHERE (c:Blubb)-[:PREDECESSOR]->( {objectState: "DEPLOYING"})
            System.out.println(tenantSupport.withTenant(line));
            break;
        }
    }

    @Test
    public void addTenant() {
        String cypher = "MATCH (c:IdEntity:ConfigEntity {objectState:'COMMITTED'}) WHERE ((c)-[:PREDECESSOR]->({objectState:'DEPLOYING'})) call some.procedure(a)";
//        String cypher = "MATCH (c:IdEntity:ConfigEntity {objectState:'COMMITTED'})";

        TenantSupport support = new TenantSupport("Client1");

        System.out.println(cypher);
        System.out.println(support.withTenant(cypher));
        //        String expectedCypherWithTenant = "MATCH (u:User:Client1)\nRETURN [(u:Client1)-[:REL]->(s:Something:Client1) | [u, s]]";
//        assertThat(support.withTenant(cypher), equalTo(expectedCypherWithTenant));
    }

}

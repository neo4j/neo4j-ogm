package org.neo4j.ogm.drivers.embedded.request;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class TenantSupportTest {


    @Test
    public void onlyAllowCorrectLabelTypesForTenant() {

    }

    @Test
    public void addTenant() {
        String cypher = "MATCH (u:User) RETURN [(u)-[:REL]->(s:Something) | [u, s]]";

        TenantSupport support = new TenantSupport("Client1");

        String expectedCypherWithTenant = "MATCH (u:User:Client1)\nRETURN [(u:Client1)-[:REL]->(s:Something:Client1) | [u, s]]";
        assertThat(support.withTenant(cypher), equalTo(expectedCypherWithTenant));
    }

}

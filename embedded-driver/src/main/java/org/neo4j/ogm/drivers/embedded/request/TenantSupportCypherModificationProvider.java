package org.neo4j.ogm.drivers.embedded.request;

import java.util.function.Function;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.spi.CypherModificationProvider;

public class TenantSupportCypherModificationProvider implements CypherModificationProvider {
    @Override public Function<String, String> getCypherModifcation(Configuration configuration) {
        TenantSupport tenantSupport = TenantSupport.supportFor("Test");
        return tenantSupport::withTenant;
    }
}

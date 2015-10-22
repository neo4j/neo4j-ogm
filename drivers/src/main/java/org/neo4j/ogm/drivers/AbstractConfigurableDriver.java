package org.neo4j.ogm.drivers;

import org.neo4j.ogm.api.authentication.UsernamePasswordCredentials;
import org.neo4j.ogm.api.config.Configuration;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.transaction.TransactionManager;

/**
 * @author vince
 */
public abstract class AbstractConfigurableDriver implements Driver {

    protected Configuration driverConfig;
    protected TransactionManager transactionManager;

    @Override
    public void configure(Configuration config) {
        this.driverConfig = config;
        setCredentials();
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object getConfig(String key) {
        return driverConfig.getConfig(key);
    }

    @Override
    public void setConfig(String key, Object value) {
        driverConfig.setConfig(key, value);
    }

    private void setCredentials() {
        if (driverConfig.getConfig("credentials") == null) {
            String username = (String) driverConfig.getConfig("username");
            String password = (String) driverConfig.getConfig("password");
            if (username != null && password != null) {
                driverConfig.setConfig("credentials", new UsernamePasswordCredentials(username, password));

            }
        }
    }
}

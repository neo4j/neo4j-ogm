package org.neo4j.ogm.driver.bolt.driver;

import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.request.Request;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.api.transaction.TransactionManager;
import org.neo4j.ogm.config.ComponentConfiguration;
import org.neo4j.ogm.driver.bolt.request.BoltRequest;
import org.neo4j.ogm.driver.bolt.transaction.BoltTransaction;

/**
 * @author vince
 */
public class BoltDriver implements Driver {

    private Session transport;
    private ComponentConfiguration driverConfig;
    private TransactionManager transactionManager;

    public BoltDriver() {
        configure(new ComponentConfiguration("bolt.driver.properties"));
    }


    @Override
    public void configure(ComponentConfiguration config) {
        this.driverConfig = config;
        this.transport = GraphDatabase.driver((String) config.getConfig("server")).session();
    }

    @Override
    public Transaction newTransaction() {
        return new BoltTransaction(transactionManager, transport);
    }

    @Override
    public void close() {
        // cannot detect if transport is already closed, so explicitly set to null.
        if (transport != null) {
            transport.close();
            transport = null;
        }
    }

    @Override
    public Object getConfig(String key) {
        return driverConfig.getConfig(key);
    }

    @Override
    public Request requestHandler() {
        return new BoltRequest(transport);
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
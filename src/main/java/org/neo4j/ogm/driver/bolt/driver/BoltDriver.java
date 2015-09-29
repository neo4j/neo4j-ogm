package org.neo4j.ogm.driver.bolt.driver;

import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.bolt.request.BoltRequest;
import org.neo4j.ogm.driver.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public class BoltDriver implements Driver {

    private Session transport;
    private DriverConfig driverConfig;
    private TransactionManager transactionManager;

    public BoltDriver() {
        configure(new DriverConfig("driver.properties.bolt"));
    }


    @Override
    public void configure(DriverConfig config) {
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
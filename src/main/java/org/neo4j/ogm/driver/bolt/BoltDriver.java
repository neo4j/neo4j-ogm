package org.neo4j.ogm.driver.bolt;

import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public class BoltDriver implements Driver {

    private Session session;
    private DriverConfig driverConfig;
    private TransactionManager transactionManager;

    public BoltDriver() {
        configure(new DriverConfig("driver.properties.bolt"));
    }


    @Override
    public void configure(DriverConfig config) {
        this.driverConfig = config;
        this.session = GraphDatabase.driver((String) config.getConfig("server")).session();
        this.driverConfig.setConfig("session", session);
    }

    @Override
    public Transaction newTransaction() {
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public void close() {
        // cannot detect if session is already closed, so explicitly set to null.
        if (session != null) {
            session.close();
            session = null;
            this.driverConfig.setConfig("session", null);
        }
    }

    @Override
    public Object getConfig(String key) {
        return driverConfig.getConfig(key);
    }

    @Override
    public Request requestHandler() {
        return new BoltRequest(session);
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
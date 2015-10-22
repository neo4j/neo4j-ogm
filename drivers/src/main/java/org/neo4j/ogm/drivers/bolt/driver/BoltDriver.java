package org.neo4j.ogm.drivers.bolt.driver;

import org.neo4j.driver.Session;
import org.neo4j.ogm.api.config.Configuration;
import org.neo4j.ogm.api.request.Request;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.drivers.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.bolt.request.BoltRequest;
import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;

/**
 * @author vince
 */
public class BoltDriver extends AbstractConfigurableDriver {

    private Session transport;


    public BoltDriver() {
        configure(new Configuration("bolt.driver.properties"));
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
    public Request requestHandler() {
        return new BoltRequest(transport);
    }

}
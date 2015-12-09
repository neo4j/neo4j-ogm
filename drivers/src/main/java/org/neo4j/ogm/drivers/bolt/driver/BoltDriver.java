package org.neo4j.ogm.drivers.bolt.driver;

import org.neo4j.driver.Session;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.drivers.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.bolt.request.BoltRequest;
import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author vince
 */
public class BoltDriver extends AbstractConfigurableDriver {

    private Session boltSession;


    public BoltDriver() {
        configure(new DriverConfiguration(new Configuration("bolt.driver.properties")));
    }

    public BoltDriver(DriverConfiguration driverConfiguration) {
        configure(driverConfiguration);
    }

    @Override
    public Transaction newTransaction() {
        return new BoltTransaction(transactionManager, boltSession);
    }

    @Override
    public void close() {
        // cannot detect if boltSession is already closed, so explicitly set to null.
        if (boltSession != null) {
            boltSession.close();
            boltSession = null;
        }
    }

    @Override
    public Request request() {
        return new BoltRequest(boltSession);
    }

}
package org.neo4j.ogm.driver;

import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.service.LoadableService;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author vince
 */
public interface Driver extends LoadableService {

    void configure(DriverConfiguration config);

    Transaction newTransaction();

    void close();

    Request request();

    void setTransactionManager(TransactionManager tx);

    DriverConfiguration getConfiguration();
}

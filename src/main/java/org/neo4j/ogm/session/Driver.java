package org.neo4j.ogm.session;

import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public interface Driver {

    public void configure(DriverConfig config);
    public Object getConfig(String key);
    public Transaction newTransaction();
    public void close();
    public Request requestHandler();
    public void setTransactionManager(TransactionManager tx);


}

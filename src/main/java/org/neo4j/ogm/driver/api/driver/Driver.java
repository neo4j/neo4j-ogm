package org.neo4j.ogm.driver.api.driver;

import org.neo4j.ogm.driver.api.request.Request;
import org.neo4j.ogm.driver.api.transaction.Transaction;
import org.neo4j.ogm.driver.api.transaction.TransactionManager;
import org.neo4j.ogm.driver.impl.driver.DriverConfig;

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

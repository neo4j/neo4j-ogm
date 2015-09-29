package org.neo4j.ogm.driver;

import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public interface Driver {

    public void configure(DriverConfig config);
    public Object getConfig(String key);
    public Transaction newTransaction(MappingContext context, TransactionManager tx);
    public void close();
    public Request requestHandler();
    public TransactionManager transactionManager();
    public void setTransactionManager(TransactionManager tx);


}

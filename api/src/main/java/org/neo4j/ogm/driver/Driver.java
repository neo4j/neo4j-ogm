package org.neo4j.ogm.driver;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.service.LoadableService;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author vince
 */
public interface Driver extends LoadableService {

    public void configure(Configuration config);
    public Object getConfig(String key);
    public void setConfig(String key, Object value);
    public Transaction newTransaction();
    public void close();
    public Request requestHandler();
    public void setTransactionManager(TransactionManager tx);


}

package org.neo4j.ogm.driver;

import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.request.Neo4jRequest;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public interface Driver<T> extends Neo4jRequest<T> {

    public void configure(DriverConfig config);
    public void rollback(Transaction tx);
    public void commit(Transaction tx);
    public Transaction openTransaction(MappingContext context, TransactionManager tx, boolean autoCommit);
    public void close();
}

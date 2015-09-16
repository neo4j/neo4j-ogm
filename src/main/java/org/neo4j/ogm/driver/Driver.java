package org.neo4j.ogm.driver;

import org.neo4j.ogm.authentication.Neo4jCredentials;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.session.request.Neo4jRequest;
import org.neo4j.ogm.session.transaction.Transaction;

/**
 * @author vince
 */
public interface Driver<T> extends Neo4jRequest<T> {

    public void configure(DriverConfig config);
    public void rollback(Transaction tx);
    public void commit(Transaction tx);
    public String newTransactionUrl(String host);
    public void authorize(Neo4jCredentials credentials);
    public void close();
}

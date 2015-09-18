package org.neo4j.ogm.driver.bolt;

import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author vince
 */
public class BoltDriver implements Driver<String> {


    private DriverConfig driverConfig;

    public BoltDriver() {
        this.driverConfig = new DriverConfig("driver.properties.bolt");
    }


    @Override
    public void configure(DriverConfig config) {
        this.driverConfig = config;
    }

    @Override
    public void rollback(Transaction tx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void commit(Transaction tx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Transaction openTransaction(MappingContext context, TransactionManager tx, boolean autoCommit) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Neo4jResponse<String> execute(String cypher) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getConfig(String key) {
        return driverConfig.getConfig(key);
    }
}

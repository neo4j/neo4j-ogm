package org.neo4j.ogm.driver.embedded;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.request.RequestHandler;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author vince
 */
public class EmbeddedDriver implements Driver<String> {

    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    private GraphDatabaseService graphDb = null;
    private DriverConfig driverConfig;

    public EmbeddedDriver() {
        configure(new DriverConfig("driver.properties.embedded"));
    }

    /**
     * Registers a shutdown hook for the Neo4j instance so that it
     * shuts down nicely when the VM exits (even if you "Ctrl-C" the
     * running application).
     *
     * @param graphDb the embedded instance to shutdown
     */
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    @Override
    public synchronized void configure(DriverConfig config) {

        this.driverConfig = config;

        if (graphDb != null) {
            logger.warn("Instance is being re-configured");
            graphDb.shutdown();
        }

        String storeDir = (String) config.getConfig("neo4j.store");

        // TODO: String ha = config.getConfig("ha");

        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder( storeDir )
                .newGraphDatabase();

        registerShutdownHook(graphDb);

        config.setConfig("graphDb", graphDb);
    }

    @Override
    public Transaction openTransaction(MappingContext context, TransactionManager tx, boolean autoCommit) {
        return new EmbeddedTransaction(context, tx, autoCommit, graphDb);
    }


    @Override
    public void close() {
        if (graphDb != null) {
            graphDb.shutdown();
        }
    }

    @Override
    public RequestHandler requestHandler() {
        //return null;  //To change body of implemented methods use File | Settings | File Templates.
        throw new RuntimeException("Not implemented!");
    }

    @Override
    public Neo4jResponse<String> execute(String jsonStatements) {
        return new EmbeddedResponse(graphDb.execute(jsonStatements));
    }

    @Override
    public Neo4jResponse<String> execute(String cypher, Map<String, Object> parameters) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getConfig(String key) {
        return driverConfig.getConfig(key);
    }


}

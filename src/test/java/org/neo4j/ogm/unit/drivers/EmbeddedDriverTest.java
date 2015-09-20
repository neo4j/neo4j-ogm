package org.neo4j.ogm.unit.drivers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.embedded.EmbeddedDriver;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.response.GraphModelResponse;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.driver.embedded.EmbeddedTransaction;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

import static org.junit.Assert.*;

/**
 * @author vince
 */
public class EmbeddedDriverTest {

    private Driver driver;
    private MappingContext mappingContext;
    private MetaData metaData;
    private TransactionManager txManager;

    @Before
    public void setUp() {

        driver = new EmbeddedDriver();
        metaData = new MetaData("org.neo4j.domain.bike");
        mappingContext = new MappingContext(metaData);
        txManager = new TransactionManager(driver);
    }

    @After
    public void tearDown() {
        driver.close();
    }

    @Test
    public void shouldStartEmbeddedDriver() {

        assertNotNull(driver.getConfig("graphDb"));

        GraphDatabaseService graphDatabaseService = (GraphDatabaseService) driver.getConfig("graphDb");

        assertTrue(graphDatabaseService.isAvailable(1000));
    }

    @Test
    public void shouldStopEmbeddedDriver() {

        driver.close();


        GraphDatabaseService graphDatabaseService = (GraphDatabaseService) driver.getConfig("graphDb");

        assertFalse(graphDatabaseService.isAvailable(1000));

    }

    @Test
    public void shouldObtainATransaction()  {
        Transaction tx = txManager.openTransaction(mappingContext);
        assertTrue(tx instanceof EmbeddedTransaction);
        tx.close();
    }


    @Test
    public void shouldReturnANeo4jResponseFromAnInternalResult()  {

        Transaction tx = txManager.openTransaction(mappingContext);

        Neo4jResponse<String> response = driver.execute("CREATE p=(n:ITEM {name:'item 1'})-[r:LINK {weight:4}]->(m:ITEM {sizes: [1,5,11], colours: ['red', 'green', 'blue']}) RETURN p");

        assertNotNull(response);

        tx.close();

    }

    @Test
    public void shouldDefaultNeo4jResponseToGraphModelResponse() {

        Transaction tx = txManager.openTransaction(mappingContext);

        Neo4jResponse<String> response = driver.execute("CREATE p=(n:ITEM {name:'item 1'})-[r:LINK {weight:4}]->(m:ITEM {sizes: [1,5,11], colours: ['red', 'green', 'blue']}) RETURN p");
        GraphModelResponse gmr = new GraphModelResponse(response, new ObjectMapper());

        GraphModel graphModel = gmr.next();

        gmr.close();

        tx.close();

        assertNotNull(graphModel);

        assertEquals(2, graphModel.getNodes().size());
        assertEquals(1, graphModel.getRelationships().size());

    }


}

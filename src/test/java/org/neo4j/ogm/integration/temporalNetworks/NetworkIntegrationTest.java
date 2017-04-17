package org.neo4j.ogm.integration.temporalNetworks;

import org.junit.*;
import org.neo4j.ogm.domain.temporalNetwork.ShopIN;
import org.neo4j.ogm.domain.temporalNetwork.ShopSN;
import org.neo4j.ogm.domain.temporalNetwork.ShopTimeRelation;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class NetworkIntegrationTest {
    @ClassRule
    public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.temporalNetwork").openSession(neo4jRule.url());
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test
    public void shouldReadIdentityNodeAlongWithStates() {
        // arrange

        ShopIN shopIN = new ShopIN();

        ShopSN shopState1 = new ShopSN();
        shopState1.setName("Good graph databases");
        shopState1.setDescription("We sell World's Leading Graph Database");

        ShopTimeRelation stateRel1 = new ShopTimeRelation();
        stateRel1.setFrom(10L);
        stateRel1.setTo(20L);
        stateRel1.setSourceNode(shopIN);
        stateRel1.setTargetNode(shopState1);

        ShopSN shopState2 = new ShopSN();
        shopState2.setName("Great graph databases");
        shopState2.setDescription("We sell World's Leading Graph Database");

        ShopTimeRelation stateRel2 = new ShopTimeRelation();
        stateRel2.setFrom(20L);
        stateRel2.setTo(Long.MAX_VALUE);
        stateRel2.setSourceNode(shopIN);
        stateRel2.setTargetNode(shopState2);

        Set<ShopTimeRelation> relations = new HashSet<>();
        relations.add(stateRel1);
        relations.add(stateRel2);

        shopIN.setStates(relations);

        session.save(shopIN);
        session.clear();

        // act
        ShopIN loadedShopIN = session.load(ShopIN.class, shopIN.getId(), 3);

        // assert
        Assert.assertEquals(2, loadedShopIN.getStates().size());

    }
}

package org.springframework.data.neo4j.integration.repositories;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.integration.repositories.repo.PersistenceContextInTheSamePackage;
import org.springframework.data.neo4j.integration.repositories.domain.User;
import org.springframework.data.neo4j.integration.repositories.repo.UserRepository;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;

@ContextConfiguration(classes = {PersistenceContextInTheSamePackage.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RepoScanningTest extends WrappingServerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected int neoServerPort() {
        return 7879;
    }

    @Test
    public void enableNeo4jRepositoriesShouldScanSelfPackageByDefault() {
        User user = new User("Michal");
        userRepository.save(user);

        assertSameGraph(getDatabase(), "CREATE (u:User {name:'Michal'})");
    }
}

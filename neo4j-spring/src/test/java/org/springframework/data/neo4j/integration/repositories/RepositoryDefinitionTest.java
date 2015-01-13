package org.springframework.data.neo4j.integration.repositories;

import com.graphaware.test.integration.WrappingServerIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.integration.repositories.context.PersistenceContext;
import org.springframework.data.neo4j.integration.repositories.domain.Movie;
import org.springframework.data.neo4j.integration.repositories.repo.MovieRepository;
import org.springframework.data.neo4j.util.IterableUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.graphaware.test.unit.GraphUnit.assertSameGraph;
import static org.junit.Assert.assertEquals;

@ContextConfiguration(classes = {PersistenceContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
@Ignore //todo
public class RepositoryDefinitionTest extends WrappingServerIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Override
    protected int neoServerPort() {
        return 7879;
    }

    @Test
    public void shouldProxyAndAutoImplementRepositoryDefinitionAnnotatedRepo() {
        Movie movie = new Movie("PF");
        movieRepository.save(movie);

        assertSameGraph(getDatabase(), "CREATE (m:Movie {name:'PF'})");

        assertEquals(1, IterableUtils.count(movieRepository.findAll()));
    }
}

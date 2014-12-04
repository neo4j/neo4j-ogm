package org.neo4j.spring.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.spring.domain.A;
import org.neo4j.spring.domain.B;
import org.neo4j.spring.repositories.GraphRepository;
import org.neo4j.spring.repositories.Neo4jRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={org.neo4j.spring.repository.ApplicationContext.class})

public class RepositoriesTest {

    // the Neo4jRepositories are wired by Spring
    // using componentScan @Repository and the default behaviour
    // that doesn't require you to wire a bean manually.

    // In this example, we create two 'class-per-repo' repositories
    // the underlying implementation for each however is the same instance
    // which means that they share a common session state.

    // If you don't want to do 'class-per-repo' pattern, you can
    // just wire in a Neo4jRepository without declaring a generic type.
    //
    // Please note we don't directly support methods of CrudRepository<T, Long>
    // because of problems associated with multi-level Type Erasure and the
    // way Spring binds the <Repository>Impl to Repository<T>. This means we
    // are not backwardly compatible with existing SDN at the moment.

    @Autowired
    private Neo4jRepository<A> ARepository;

    @Autowired
    private Neo4jRepository<B> BRepository;


    // these repos are wired manually
    @Autowired
    GraphRepository<A> ARepo;

    @Autowired
    GraphRepository<B> BRepo;

    @Test
    public void testSaveTwoObjectsUsingNeo4jRepositories() {

        A a = new A();
        B b = new B();

        ARepository.save(a);
        BRepository.save(b);

        // prove the underlying implementing class is the same for both repos.
        // we're just using the "convenience" of compile-time generics
        // to enforce type distinctions in code.
        assertTrue(ARepository.equals(BRepository));

        assertNotNull(a.getId());
        assertNotNull(b.getId());

    }

    @Test
    public void testSaveTwoObjectsUsingGraphRepositories() {

        A a = new A();
        B b = new B();

        ARepo.save(a);
        BRepo.save(b);

        assertNotNull(a.getId());
        assertNotNull(b.getId());

    }


}

package org.neo4j.spring.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.spring.domain.A;
import org.neo4j.spring.domain.B;
import org.neo4j.spring.repositories.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={org.neo4j.spring.repository.ApplicationContext.class})

public class RepositoriesTest {

    // these repos are wired manually
    @Autowired
    GraphRepository<A> ARepo;

    @Autowired
    GraphRepository<B> BRepo;

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

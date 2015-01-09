package org.neo4j.spring.integration.helloworld.repo;

import org.neo4j.spring.integration.helloworld.domain.World;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldRepository extends GraphRepository<World> {}

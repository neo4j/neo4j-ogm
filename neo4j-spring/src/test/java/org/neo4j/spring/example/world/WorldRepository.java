package org.neo4j.spring.example.world;

import org.neo4j.spring.domain.World;
import org.neo4j.spring.repositories.GraphRepository;

public interface WorldRepository extends GraphRepository<World> {}

package org.neo4j.ogm.domain.gh952;

import java.util.UUID;

import org.neo4j.ogm.id.IdStrategy;

public class UuidGenerationStrategy implements IdStrategy {

	@Override
	public Object generateId(Object entity) {
		return UUID.randomUUID().toString();
	}

}

package org.neo4j.ogm.strategy;

import org.neo4j.ogm.metadata.PersistentField;

public interface EntityAccessStrategyFactory {

    EntityAccessStrategy forPersistentField(PersistentField pf);

    EntityAccessStrategy forType(Class<?> type);

}

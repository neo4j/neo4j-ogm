package org.neo4j.ogm.strategy;

import org.neo4j.ogm.metadata.PersistentField;

public interface EntityAccessFactory {

    EntityAccess forPersistentField(PersistentField pf);

    EntityAccess forType(Class<?> type);

}

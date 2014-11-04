package org.neo4j.ogm.session;

import org.graphaware.graphmodel.neo4j.Property;

import java.util.Collection;

public interface Session {

    <T> T load(Class<T> type, Long id);

    <T> Collection<T> loadByProperties(Class<T> type, Collection<Property> properties);

    <T> Collection<T> load(Class<T> type);

}

package org.neo4j.ogm.session.query;

import java.util.Map;

public interface Query<T> extends AutoCloseable {

    Query<T> execute();

    T next();

}

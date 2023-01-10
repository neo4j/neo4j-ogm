package org.neo4j.ogm.context;

import java.util.Optional;

/**
 * @author Aliaksei Bialiauski
 */
public interface Reminiscence {

    Optional<EntitySnapshot> snapshot(Object object, Long id);

    void remember(Object object, Long id);

    boolean remembered(Object object, Long id);

    void clear();
}

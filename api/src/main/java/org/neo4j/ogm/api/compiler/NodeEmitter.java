package org.neo4j.ogm.api.compiler;

import org.neo4j.ogm.entityaccess.EntityAccessStrategy;
import org.neo4j.ogm.metadata.info.ClassInfo;

/**
 * @author vince
 */
public interface NodeEmitter extends CypherEmitter, Comparable<NodeEmitter> {

    String reference();

    // todo ClassInfo must be an interface, or moved into api project

    NodeEmitter mapProperties(Object entity, ClassInfo classInfo, EntityAccessStrategy entityAccessStrategy);

    NodeEmitter addLabels(Iterable<String> labels);


}

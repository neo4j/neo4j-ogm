package org.neo4j.ogm.api.compiler;

/**
 * @author vince
 */
public interface NodeEmitter extends CypherEmitter, Comparable<NodeEmitter> {

    String reference();

    NodeEmitter addProperty(String key, Object value);

    NodeEmitter addLabels(Iterable<String> labels);

}

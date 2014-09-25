package org.neo4j.ogm.metadata;

import org.graphaware.graphmodel.Edge;
import org.graphaware.graphmodel.Vertex;

/*
 * Will probably have an implementation that works on zero-arg constructors by default.
 */
public interface ObjectCreator {

    <T> T instantiateObjectMappedTo(Vertex vertex);

    <T> T instantiateObjectMappedTo(Edge edge);

}

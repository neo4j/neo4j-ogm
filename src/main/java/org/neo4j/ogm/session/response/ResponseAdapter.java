package org.neo4j.ogm.session.response;

/**
 * This adapter interface should be implemented by all drivers that do not return a response
 * in a JSON format.
 *
 * @author vince
 */
public interface ResponseAdapter<F, T> {
     F adapt(T response);
}

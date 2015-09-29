package org.neo4j.ogm.session.result;

/**
 * This adapter interface should be implemented by all drivers
 *
 * @author vince
 */
public interface ResultAdapter<F, T> {
     T adapt(F result);
}

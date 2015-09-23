package org.neo4j.ogm.session.response.adapter;

/**
 * This adapter interface should be implemented by all drivers
 *
 * @author vince
 */
public interface ResponseAdapter<F, T> {
     T adapt(F response);
}

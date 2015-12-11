package org.neo4j.ogm.context;

import org.neo4j.ogm.response.Response;

/**
 * Specification for an object-model mapper, which can map a model M onto arbitrary Java objects.
 *
 * @param <M> The Response model
 * @author Vince Bickers
 */

public interface ResponseMapper<M> {
    /**
     * Maps the data representation in the given response onto instances of <code>T</code>.
     *
     * @param type The {@link Class} defining the type to which each entities in the response should be mapped
     * @param response The {@link org.neo4j.ogm.response.Response} object containing the data to map onto the objects
     * @return An {@link Iterable} of type <code>T</code> containing relevant data extracted from the response
     */
    <T> Iterable<T> map(Class<T> type, Response<M> response);
}

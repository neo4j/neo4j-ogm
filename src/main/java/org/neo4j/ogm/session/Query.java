package org.neo4j.ogm.session;

import java.util.Collection;

public interface Query {

    /**
     * fetch a single object with the specified id
     * @param id the id of the object to find
     * @return a Cypher expression
     */
    String findOne(Long id);

    /**
     * fetch all objects with the specified ids
     * @param ids the ids of the objects to find
     * @return a Cypher expression
     */
    String findAll(Collection<Long> ids);

    /**
     * fetch all objects
     * @return a Cypher expression
     */
    String findAll();

    /**
     * fetch all objects with the specified label
     * @param label the labels attached to the objects
     * @return a Cypher expression
     */
    String findByLabel(String label);

    /**
     * delete an object
     * @param id the id of the object to delete
     * @return a Cypher expression
     */
    String delete(Long id);

    /**
     * delete all objects with the specified ids
     * @param ids the ids of the objects to delete
     * @return a Cypher expression
     */
    String deleteAll(Collection<Long> ids);

    /**
     * purge the database
     * @return a Cypher expression
     */
    String purge();

    /**
     * delete all objects having the supplied labels
     * @param label the label for the objects to be deleted
     * @return a Cypher expression
     */
    String deleteByLabel(String label);

}

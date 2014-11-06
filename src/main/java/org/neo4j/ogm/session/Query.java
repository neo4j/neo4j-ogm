package org.neo4j.ogm.session;

import java.util.Collection;

public interface Query {

    /**
     * fetch a single object with the specified id
     * @param id
     */
    String findOne(Long id);

    /**
     * fetch all objects with the specified ids
     * @param ids
     */
    String findAll(Collection<Long> ids);

    /**
     * fetch all objects with the specified labels
     * @param labels
     */
    String findByLabel(Collection<String> labels);


}

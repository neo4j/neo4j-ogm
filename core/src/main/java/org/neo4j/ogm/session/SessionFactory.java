/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session;


import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.Components;

/**
 * Used to create {@link Session} instances for interacting with Neo4j.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class SessionFactory {

    private final MetaData metaData;

    /**
     * Constructs a new {@link SessionFactory} by initialising the object-graph mapping meta-data from the given list of domain
     * object packages.
     * <p>
     * The package names passed to this constructor should not contain wildcards or trailing full stops, for example,
     * "org.springframework.data.neo4j.example.domain" would be fine.  The default behaviour is for sub-packages to be scanned
     * and you can also specify fully-qualified class names if you want to cherry pick particular classes.
     * </p>
     *
     * @param packages The packages to scan for domain objects
     */
    public SessionFactory(String... packages) {
        this.metaData = new MetaData(packages);
    }


    /**
     * Retrieves the meta-data that was built up when this {@link SessionFactory} was constructed.
     *
     * @return The underlying {@link MetaData}
     */
    public MetaData metaData() {
        return metaData;
    }

    /**
     * Opens a new Neo4j mapping {@link Session} using the specified Driver
     * The driver should be configured to connect to the database using the appropriate
     * DriverConfig
     *
     * @param driver The driver to use to connect to Neo4j
     * @return A new {@link Session}
     */
    public Session openSession(Driver driver) {
        return new Neo4jSession(metaData, driver);
    }

    /**
     * Opens a new Neo4j mapping {@link Session} using the Driver specified in the OGM configuration
     * The driver should be configured to connect to the database using the appropriate
     * DriverConfig
     *
     * @return A new {@link Session}
     */
    public Session openSession() {
        return new Neo4jSession(metaData, Components.driver());
    }

    /**
     * Opens a new {@link Session} using the {@link Configuration} supplied.
     * @param configuration the Configuration to use
     * @return A new {@link Session}
     */
    public Session openSession(Configuration configuration) {
        Components.configure(configuration);
        return new Neo4jSession(metaData, Components.driver());

    }

}

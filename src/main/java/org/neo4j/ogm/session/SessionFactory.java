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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.Drivers;
import org.neo4j.ogm.metadata.MetaData;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Used to create {@link Session} instances for interacting with Neo4j.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class SessionFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();
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
     * Opens a new Neo4j mapping {@link Session} against the specified Neo4j database.
     * The url may optionally contain the username and password to use while authenticating
     * for example, http://username:password@neo-server:port
     * Otherwise, if authentication is required, the username and password will be read from System properties.
     *
     * The connection is opened using the default driver protocol.
     *
     * @param url The base URL of the Neo4j database with which to communicate.
     * @return A new {@link Session}
     */
    public Session openSession(String url) {
        try {
            URI uri = new URI(url);
            String username = null;
            String password = null;
            String uriStr = uri.toString();
            String auth = uri.getUserInfo();
            if (auth != null && !auth.trim().isEmpty()) {
                username=auth.split(":")[0];
                password=auth.split(":")[1];

                uriStr = uri.getScheme() + "://" + uri.toString().substring(uri.toString().indexOf(auth) + auth.length()+1);
            }

            if(username!=null && password!=null) {
                return new Neo4jSession(metaData, uriStr, Drivers.DEFAULT, objectMapper, new UsernamePasswordCredentials(username, password));

            }
            return new Neo4jSession(metaData, uriStr, Drivers.DEFAULT, objectMapper);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a new Neo4j mapping {@link Session} against the specified Neo4j database.
     *
     * The connection is opened using the default driver protocol.
     *
     * @param url The base URL of the Neo4j database with which to communicate
     * @param username The username to authenticate with
     * @param password The password to authenticate with
     * @return A new {@link Session}
     */
    public Session openSession(String url, String username, String password) {
        return new Neo4jSession(metaData, url, Drivers.DEFAULT, objectMapper, new UsernamePasswordCredentials(username, password));
    }

    /**
     * Opens a new Neo4j mapping {@link Session} against the specified Neo4j database using the specified driver.
     *
     * @param url The base URL of the Neo4j database with which to communicate
     * @param driver the driver to use to connect to neo4k
     * @return A new {@link Session}
     */

    public Session openSession(String url, Driver driver) {
        try {
            URI uri = new URI(url);
            String username = null;
            String password = null;
            String uriStr = uri.toString();
            String auth = uri.getUserInfo();
            if (auth != null && !auth.trim().isEmpty()) {
                username=auth.split(":")[0];
                password=auth.split(":")[1];

                uriStr = uri.getScheme() + "://" + uri.toString().substring(uri.toString().indexOf(auth) + auth.length()+1);
            }

            if(username!=null && password!=null) {
                return new Neo4jSession(metaData, uriStr, driver, objectMapper, new UsernamePasswordCredentials(username, password));

            }
            return new Neo4jSession(metaData, uriStr, driver, objectMapper);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a new Neo4j mapping {@link Session} against the specified Neo4j database using the specified driver.
     *
     * @param url The base URL of the Neo4j database with which to communicate
     * @param driver the driver to use to connect to neo4k
     * @param username The username to authenticate with
     * @param password The password to authenticate with
     * @return A new {@link Session}
     */
    public Session openSession(String url, String username, String password, Driver driver) {
        return new Neo4jSession(metaData, url, driver, objectMapper, new UsernamePasswordCredentials(username, password));
    }

    /**
     * Retrieves the meta-data that was built up when this {@link SessionFactory} was constructed.
     *
     * @return The underlying {@link MetaData}
     */
    public MetaData metaData() {
        return metaData;
    }

}

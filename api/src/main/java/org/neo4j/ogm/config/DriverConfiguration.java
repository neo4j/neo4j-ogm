/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.config;

import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;

import java.net.URL;

/**
 * 
 * A wrapper class for a generic {@link Configuration} that exposes the configuration for a Driver via
 * concrete methods.
 * 
 * @author vince
 */
public class DriverConfiguration {

    public static final String DRIVER = "driver";
    public static final String CREDENTIALS = "credentials";
    public static final String URI = "URI";

    private final Configuration configuration;

    public DriverConfiguration() {
        this.configuration = new Configuration();
    }

    public DriverConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public DriverConfiguration setDriverClassName(String driverClassName) {
        configuration.set(DRIVER, driverClassName);
        return this;
    }

    public DriverConfiguration setURI(String uri) {
        configuration.set(URI, uri);
        try { // if this URI is a genuine resource, see if it has an embedded userinfo and set credentials accordingly
            URL url = new URL(uri);
            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                String[] userPass = userInfo.split(":");
                setCredentials(userPass[0], userPass[1]);
            }
        } catch (Exception e) {
            ; // do nothing here. user not obliged to supply a URL, or to pass in credentials
        }
        return this;
    }

    public DriverConfiguration setCredentials(Credentials credentials) {
        configuration.set(CREDENTIALS, credentials);
        return this;
    }

    public DriverConfiguration setCredentials(String username, String password) {
        configuration.set(CREDENTIALS, new UsernamePasswordCredentials(username, password));
        return this;
    }

    /**
     * Returns the driver connection credentials, if they have been provided.
     * If a Credentials object exists, it will be returned
     * If a Credentials object does not exist, it will be created from the URI's authentication part
     * If the URI does not contain an authentication part, Credentials will be created from username/password properties
     * if they have been set.
     *
     * @return a Credentials object if one exists or can be created, null otherwise
     */
    public Credentials getCredentials() {

        if (configuration.get(CREDENTIALS) == null) {
            setURI((String) configuration.get(URI)); // set from the URI?
        }

        if (configuration.get(CREDENTIALS) == null) {
            String username = (String) configuration.get("username");
            String password = (String) configuration.get("password");
            if (username != null && password != null) {
                setCredentials(username, password); // set from username/password pair
            }
        }
        return (Credentials) configuration.get(CREDENTIALS);
    }

    public String getURI() {
        return (String)  configuration.get(URI);
    }

    public String getDriverClassName() {
        return (String) configuration.get(DRIVER);
    }

}

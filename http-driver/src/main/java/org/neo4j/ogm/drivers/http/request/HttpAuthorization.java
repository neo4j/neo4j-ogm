/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.drivers.http.request;

import org.apache.http.client.methods.HttpRequestBase;
import org.neo4j.ogm.config.Credentials;

/**
 * @author Vince Bickers
 */
public class HttpAuthorization {

    /**
     * Sets the authorisation header on the request, if credentials are present.
     * This code is backwards-compatible with versions of Neo4j prior to 2.2
     * that do not require users to be authenticated in order to connect.
     * In 2.2.0 M04 Neo4j only has support for single-user String-based authentication
     * based on username and password. As the security of Neo4j improves, it is
     * likely that other auth mechanisms will be enabled (e.g OAuth). When this
     * happens, we'll most likely need a proper AuthenticationManager object,
     * but for now, this will do.
     *
     * @param request     The HttpRequest that may need an auth header.
     * @param credentials the {@link Credentials} with which to authorise the session
     */
    public static void authorize(HttpRequestBase request, Credentials credentials) {
        if (credentials != null) {
            request.setHeader("Authorization", "Basic " + credentials.credentials());
        }
    }
}

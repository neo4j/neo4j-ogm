package org.neo4j.ogm.authentication;

import org.apache.http.client.methods.HttpRequestBase;

public class HttpRequestAuthorization {

    /**
     * Sets the authorization header on the request, if credentials are present.
     * This code is backwards-compatible with versions of Neo4j prior to 2.2
     * that do not require users to be authenticated in order to connect.
     *
     * In 2.2.0 M04 Neo4j only has support for single-user String-based authentication
     * based on username and password. As the security of Neo4j improves, it is
     * likely that other auth mechanisms will be enabled (e.g OAuth). When this
     * happens, we'll most likely need a proper AuthenticationManager object,
     * but for now, this will do.
     *
     * @param request The HttpRequest that may need an auth header.
     */
    public static void authorize(HttpRequestBase request, Neo4jCredentials credentials) {
        if (credentials != null) {
            request.setHeader("Authorization", "Basic realm=\"Neo4j\" " + credentials.credentials());
        }
    }

}

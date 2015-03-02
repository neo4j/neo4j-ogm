package org.neo4j.ogm.authentication;

import org.apache.commons.codec.binary.Base64;

public class UsernamePasswordCredentials implements Neo4jCredentials<String> {

    private String credentials;

    public UsernamePasswordCredentials(String userName, String password) {
        this.credentials = Base64.encodeBase64String(userName.concat(":").concat(password).getBytes());
    }


    @Override
    public String credentials() {
        return credentials;
    }
}

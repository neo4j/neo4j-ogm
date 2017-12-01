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

package org.neo4j.ogm.config;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Vince Bickers
 */
public class UsernamePasswordCredentials implements Credentials<String> {

    private final String credentials;
    private final String username;
    private final String password;

    public UsernamePasswordCredentials(String userName, String password) {
        if (userName == null || password == null) {
            throw new IllegalArgumentException("username or password cannot be null");
        }
        try {
            this.credentials = Base64.encodeBase64String(userName.concat(":").concat(password).getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding support required", e);
        }
        this.username = userName;
        this.password = password;
    }

    @Override
    public String credentials() {
        return credentials;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

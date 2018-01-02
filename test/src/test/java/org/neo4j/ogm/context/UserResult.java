/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vince
 */
public class UserResult {

    private String password;
    private Long id;

    private Map<String, Object> profile = new HashMap<String, Object>();

    private Map<String, Object> associatedProfile = new HashMap<String, Object>();

    public Map<String, Object> getProfile() {
        return profile;
    }

    public void setProfile(Map<String, Object> profile) {
        this.profile = profile;
    }

    public Map<String, Object> getAssociatedProfile() {
        return associatedProfile;
    }

    public void setAssociatedProfile(Map<String, Object> associatedProfile) {
        this.associatedProfile = associatedProfile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.EntityFactory;

/**
 * TODO: This test doesn't assert anything.
 *
 * @author vince
 */
public class SingleUseEntityMapperTest {

    private MetaData metaData = new MetaData("org.neo4j.ogm.context");

    //	@Test
    //	public void shouldMapFromRowModel() throws Exception {
    //
    //
    //	}

    @Test
    public void shouldMapFromMap() throws Exception {

        Collection<Object> toReturn = new ArrayList<>();
        SingleUseEntityMapper entityMapper = new SingleUseEntityMapper(metaData, new EntityFactory(metaData));

        Iterable<Map<String, Object>> results = getQueryResults();

        for (Map<String, Object> result : results) {
            toReturn.add(entityMapper.map(UserResult.class, result));
        }
    }

    private Iterable<Map<String, Object>> getQueryResults() {
        List<Map<String, Object>> results = new ArrayList<>();

        Map<String, Object> result = new HashMap<>();

        Map<String, Object> profile = new HashMap<>();

        profile.put("enterpriseId", "p.enterpriseId");
        profile.put("clidUuid", "u.clidUuid");
        profile.put("profileId", "p.clidUuid");
        profile.put("firstName", "p.firstName");
        profile.put("lastName", "p.lastName");
        profile.put("email", "u.email");
        profile.put("roles", "roles");
        profile.put("connectionType", "connectionType");

        result.put("profile", profile);
        results.add(result);

        return results;
    }
}

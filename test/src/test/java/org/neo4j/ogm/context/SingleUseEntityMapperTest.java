/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.ReflectionEntityInstantiator;

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
    public void shouldMapFromMap() {

        Collection<Object> toReturn = new ArrayList<>();
        SingleUseEntityMapper entityMapper = new SingleUseEntityMapper(metaData, new ReflectionEntityInstantiator(metaData));

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

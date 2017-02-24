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

package org.neo4j.ogm.persistence.examples.convertible;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.convertible.parametrized.JsonNode;
import org.neo4j.ogm.domain.convertible.parametrized.StringMapEntity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class ParameterizedConversionTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.convertible.parametrized").openSession();
    }

    @After
    public void tearDown() {
        session.purgeDatabase();
    }

    @Test
    public void shouldConvertParametrizedMap() {

        JsonNode jsonNode = new JsonNode();
        jsonNode.payload = Utils.map("key", "value");

        session.save(jsonNode);

        session.clear();

        JsonNode found = session.load(JsonNode.class, jsonNode.id);

        assertTrue(found.payload.containsKey("key"));
        assertEquals("value", found.payload.get("key"));

    }

	/**
	 * @see Issue 102
     */
    @Test
    public void shouldConvertParameterizedStringMap() {
        StringMapEntity entity = new StringMapEntity();
        session.save(entity);

        session.clear();

        StringMapEntity loaded = session.load(StringMapEntity.class, entity.getId());
        assertNotNull(loaded);
        assertEquals(3, loaded.getStringMap().size());
    }
}

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

package org.neo4j.ogm.persistence.types.properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.domain.properties.User;
import org.neo4j.ogm.domain.properties.UserWithInvalidPropertiesType;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link org.neo4j.ogm.annotation.Properties} annotation that tests that invalid cases throw
 * {@link MappingException}
 *
 * @author Frantisek Hartman
 */
public class InvalidPropertiesTest extends MultiDriverTestClass {

    private static Session session;

    @Test(expected = MappingException.class)
    public void shouldThrowInvalidMappingException() throws Exception {
        session = new SessionFactory(getBaseConfiguration().build(),
                UserWithInvalidPropertiesType.class.getName())
                .openSession();
    }

}

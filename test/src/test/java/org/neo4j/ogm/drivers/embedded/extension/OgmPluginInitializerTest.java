/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.drivers.embedded.extension;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.harness.ServerControls;
import org.neo4j.ogm.domain.simple.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestServer;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.test.server.HTTP;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Frantisek Hartman
 */
public class OgmPluginInitializerTest {

    private static final String TEST_PATH = "/testOgmExtension/";

    @Before
    public void setUp() throws Exception {
        TestOgmPluginInitializer.shouldInitialize = true;
    }

    @After
    public void after() throws Exception {
        TestOgmPluginInitializer.shouldInitialize = false;
    }

    @Test
    public void testOgmPluginExtension() throws Exception {

        try (ServerControls controls = TestServer.newInProcessBuilder()
            .withConfig(GraphDatabaseSettings.auth_enabled, "false")
            .withExtension(TEST_PATH, TestOgmExtension.class)
            .newServer()) {

            URI testURI = controls.httpURI().resolve(TEST_PATH);

            HTTP.Response saveResponse = HTTP.POST(testURI.toString());
            assertThat(saveResponse.status()).isEqualTo(200);

            HTTP.Response loadResponse = HTTP.GET(testURI.toString());

            assertThat(loadResponse.rawContent()).isEqualTo("[{\"id\":0,\"name\":\"new user\"}]");
        }

    }

    @Test
    public void ogmExtensionShouldUseProvidedDatabase() throws Exception {
        try (ServerControls controls = TestServer.newInProcessBuilder()
            .withConfig(GraphDatabaseSettings.auth_enabled, "false")
            .withExtension(TEST_PATH, TestOgmExtension.class)
            .newServer()) {

            URI testURI = controls.httpURI().resolve(TEST_PATH);

            GraphDatabaseService service = controls.graph();

            try (Transaction tx = service.beginTx()) {
                service.execute("CREATE (u:User {name:'new user'})");

                tx.success();
            }

            HTTP.Response loadResponse = HTTP.GET(testURI.toString());

            assertThat(loadResponse.rawContent()).isEqualTo("[{\"id\":0,\"name\":\"new user\"}]");
        }

    }

    @Path("")
    public static class TestOgmExtension {

        @Context
        private SessionFactory sessionFactory;

        private ObjectMapper objectMapper = new ObjectMapper();

        @POST
        public Response save() {

            Session session = sessionFactory.openSession();
            User user = new User("new user");
            session.save(user);

            return Response.ok().build();
        }

        @GET
        public Response load() throws JsonProcessingException {
            Session session = sessionFactory.openSession();

            Collection<User> users = session.loadAll(User.class);

            return Response
                .ok()
                .entity(objectMapper.writeValueAsString(users))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
        }

    }

    public static class TestOgmPluginInitializer extends OgmPluginInitializer {

        public static boolean shouldInitialize = false;

        public TestOgmPluginInitializer() {
            super(User.class.getPackage().getName());
        }

        @Override
        public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration config) {
            if (shouldInitialize) {
                return super.start(graphDatabaseService, config);
            } else {
                return Collections.emptySet();
            }
        }
    }

}

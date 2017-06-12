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

package org.neo4j.ogm.drivers.embedded.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.domain.simple.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.test.server.HTTP;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Frantisek Hartman
 */
public class OgmPluginInitializerTest {

    private static final String TEST_PATH = "/testOgmExtension/";
    private static final String PLUGIN_LIFECYCLE = "target/test-classes/META-INF/services/org.neo4j.server.plugins.PluginLifecycle";

    // Create and delete the plugin lifecycle file so it doesn't get loaded with other tests

    @Before
    public void setUp() throws Exception {
        new File("target/test-classes/META-INF/services/").mkdirs();
        FileWriter out = new FileWriter(PLUGIN_LIFECYCLE);
        out.write(TestOgmPluginLifecycle.class.getName());
        out.close();
    }

    @After
    public void after() throws Exception {
        new File(PLUGIN_LIFECYCLE).delete();
    }

    @Test
    public void testOgmPluginExtension() throws Exception {

        try (ServerControls controls = TestServerBuilders.newInProcessBuilder()
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

    public static class TestOgmPluginLifecycle extends OgmPluginInitializer {
        public TestOgmPluginLifecycle() {
            super(User.class.getName());
        }

    }

}

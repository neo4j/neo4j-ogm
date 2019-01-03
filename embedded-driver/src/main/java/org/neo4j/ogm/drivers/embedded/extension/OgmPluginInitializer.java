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
package org.neo4j.ogm.drivers.embedded.extension;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.server.plugins.Injectable;
import org.neo4j.server.plugins.PluginLifecycle;

/**
 * PluginLifecycle helper for use with Neo4j unmanaged extensions
 * To create an unmanaged extension
 * - subclass this initializer, provide packages as you would provide to SessionFactory <br>
 * - create file
 * {@code META-INF/services/org.neo4j.server.plugins.PluginLifecycle}
 * in your plugin jar and list the name of the class there <br>
 * - use {@code @Context} to inject SessionFactory to extension resource <br>
 * NOTE: you also need to list your extension resource in {@code dbms.unmanaged_extension_classes} property
 *
 * @author Frantisek Hartman
 * @since 3.0
 */
public abstract class OgmPluginInitializer implements PluginLifecycle {

    protected final String packages;

    protected SessionFactory sessionFactory;

    public OgmPluginInitializer(String packages) {
        this.packages = packages;
    }

    @Override
    public Collection<Injectable<?>> start(GraphDatabaseService graphDatabaseService, Configuration config) {
        EmbeddedDriver embeddedDriver = new EmbeddedDriver(graphDatabaseService);
        sessionFactory = createSessionFactory(embeddedDriver);
        return Arrays.asList(new OgmInjectable<>(sessionFactory, SessionFactory.class));
    }

    protected SessionFactory createSessionFactory(EmbeddedDriver embeddedDriver) {
        return new SessionFactory(embeddedDriver, packages);
    }

    @Override
    public void stop() {
        // do no close SessionFactory here
        // would close the database which has been provided by the server itself
    }

    public static class OgmInjectable<T> implements Injectable<T> {

        private final Class<T> injectableClass;
        private final T injectable;

        public OgmInjectable(T injectable, Class<T> injectableClass) {
            this.injectable = injectable;
            this.injectableClass = injectableClass;
        }

        @Override
        public T getValue() {
            return injectable;
        }

        @Override
        public Class<T> getType() {
            return injectableClass;
        }
    }
}

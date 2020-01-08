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
package org.neo4j.ogm.config;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This class loader delegates to a list of class loaders, which is by default a singleton list containing the class loader
 * that has been used to load this class.
 * <p>
 * Before consulting the delegates, the current threads class loader is used (if available). When neither the class loader
 * nor any of the delegates is able to resolve a class or resource, the system class loader is used.
 *
 * @author Michael J. Simons
 */
final class DelegatingClassLoader extends ClassLoader {

    private final ClassLoader[] delegates;

    DelegatingClassLoader() {

        this(Collections.singletonList(DelegatingClassLoader.class.getClassLoader()));
    }

    DelegatingClassLoader(Collection<ClassLoader> delegates) {

        super(null); // We don't want a parent delegating mechanism here
        this.delegates = delegates.toArray(new ClassLoader[delegates.size()]);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        Iterator<ClassLoader> classLoaders = getClassLoaders();
        while (classLoaders.hasNext()) {
            ClassLoader delegate = classLoaders.next();
            try {
                return delegate.loadClass(name);
            } catch (ClassNotFoundException e) {
            }
        }

        return super.findClass(name);
    }

    @Override
    protected URL findResource(String name) {

        Iterator<ClassLoader> classLoaders = getClassLoaders();
        while (classLoaders.hasNext()) {
            ClassLoader delegate = classLoaders.next();
            URL resource = delegate.getResource(name);
            if (resource != null) {
                return resource;
            }
        }

        return super.findResource(name);
    }

    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {

        Set<URL> resources = new LinkedHashSet<>();
        Iterator<ClassLoader> classLoaders = getClassLoaders();
        while (classLoaders.hasNext()) {
            ClassLoader delegate = classLoaders.next();
            resources.addAll(Collections.list(delegate.getResources(name)));
        }

        return Collections.enumeration(resources);
    }

    private Iterator<ClassLoader> getClassLoaders() {

        return new Iterator<ClassLoader>() {

            private int index = 0;
            private ClassLoader currentContextClassLoader = Thread.currentThread().getContextClassLoader();
            private ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

            @Override
            public boolean hasNext() {
                return currentContextClassLoader != null || systemClassLoader != null || index < delegates.length;
            }

            @Override
            public ClassLoader next() {
                ClassLoader nextClassLoader;
                if (currentContextClassLoader != null) {
                    nextClassLoader = currentContextClassLoader;
                    currentContextClassLoader = null;
                } else if (index < delegates.length) {
                    nextClassLoader = delegates[index++];
                } else if (systemClassLoader != null) {
                    nextClassLoader = systemClassLoader;
                    systemClassLoader = null;
                } else {
                    throw new NoSuchElementException();
                }
                return nextClassLoader;
            }
        };
    }
}

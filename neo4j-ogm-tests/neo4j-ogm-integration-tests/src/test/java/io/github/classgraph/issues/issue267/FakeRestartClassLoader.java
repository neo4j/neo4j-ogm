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
package io.github.classgraph.issues.issue267;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * This class loader would normally live under {@link org.neo4j.ogm.metadata.DomainInfoTest}, but ClassGraph removed
 * the ability to add custom handlers. This one hooks into {@code nonapi.io.github.classgraph.classloaderhandler.ParentLastDelegationOrderTestClassLoaderHandler}.
 * This is sadly rather fragile, but a) it is for a test and b) I'd rather have the test myself again (class graph already has the test),
 * but I want to make sure it works in the context OGM indirectly via our domain info as well.
 *
 * @author Michael J. Simons
 */
public class FakeRestartClassLoader extends ClassLoader {

    public static final String DOMAIN_CLASS_TESTED = "org.neo4j.ogm.domain.policy.Person";

    private Class getClass(final String name) throws ClassNotFoundException {
        try {
            final byte[] b = loadClassFileData(name.replace('.', File.separatorChar) + ".class");
            return defineClass(name, b, 0, b.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name);
        }
    }

    @Override
    public Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
        if (name.startsWith(DOMAIN_CLASS_TESTED) || name
            .startsWith("org.neo4j.ogm.metadata.DomainInfoTestClassLoaderTestStub")) {
            Class clazz = getClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
        }
        return super.loadClass(name, resolve);
    }

    private byte[] loadClassFileData(final String name) throws IOException {
        try (final DataInputStream in = new DataInputStream(getClass().getClassLoader().getResourceAsStream(name))) {
            int size = in.available();
            byte buff[] = new byte[size];
            in.readFully(buff);
            return buff;
        }
    }

    /**
     * Don't remove, the handler calls this.
     * @return
     */
    public String getClasspath() {
        final String classfileName = DOMAIN_CLASS_TESTED.replace('.', '/') + ".class";
        final URL classfileResource = getClass().getClassLoader().getResource(classfileName);
        if (classfileResource == null) {
            throw new IllegalArgumentException("Could not find classfile " + classfileName);
        }
        final String classfilePath = classfileResource.getFile();
        final String packageRoot = classfilePath.substring(0, classfilePath.length() - classfileName.length() - 1);
        return packageRoot;
    }
}

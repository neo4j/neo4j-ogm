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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import org.neo4j.ogm.domain.policy.Person;

/**
 * Stub to be run from within different threads, asserting loaded classes can be correctly assigned when
 * reloading and "parent last" class loaders are in play.
 * The code is basically the same as the one originally contributed to ClassGraph, but adapted having OGM indirection
 * in between.
 *
 * @author Michael J. Simons
 */
public class DomainInfoTestClassLoaderTestStub {
    public void assertCorrectClassLoaders(final String parentClassLoader, final String expectedClassLoader) {

        Person a = new Person();
        // Checking the precondition here: We forced our classloader onto "everything"
        assertThat(DomainInfoTestClassLoaderTestStub.class.getClassLoader().getClass().getSimpleName())
            .isEqualTo(expectedClassLoader);
        assertThat(a.getClass().getClassLoader().getClass().getSimpleName()).isEqualTo(expectedClassLoader);

        DomainInfo classGraph = DomainInfo.create("org.neo4j.ogm.domain.policy");

        // ClassGraph is in that setup not part of the RestartClass loader. That one takes by default only
        // URLs from the current project into consideration and can only be modified by adding additional
        // directories, see https://github.com/spring-projects/spring-boot/issues/12869
        assertThat(classGraph.getClass().getClassLoader().getClass().getSimpleName()).isEqualTo(parentClassLoader);

        // Now use ClassGraph to find everything

        ClassInfo classInfo = classGraph.getClass("org.neo4j.ogm.domain.policy.Person");

        // And it should load it through the same class loader it found it with
        Class<?> aClassLoadedThroughClassGraph = classInfo.getUnderlyingClass();
        assertThat(aClassLoadedThroughClassGraph.getClassLoader().getClass().getSimpleName())
            .isEqualTo(expectedClassLoader);
        // and thus assignable
        assertThat(a.getClass().isAssignableFrom(aClassLoadedThroughClassGraph));
    }
}

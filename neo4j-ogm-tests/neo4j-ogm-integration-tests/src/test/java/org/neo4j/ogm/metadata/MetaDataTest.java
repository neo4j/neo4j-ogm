/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.gh391.ClassWithNonUniqueSimpleName;
import org.neo4j.ogm.domain.gh391.SomeContainer;
import org.neo4j.ogm.domain.gh551.ThingResult;
import org.neo4j.ogm.exception.core.AmbiguousBaseClassException;
import org.neo4j.ogm.exception.core.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class MetaDataTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataTest.class);

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.pizza",
            "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.domain.hierarchy.domain");
    }

    /**
     * A class can be found if its simple name is unique in the domain
     */
    @Test
    public void testClassInfo() {
        assertThat(metaData.classInfo("Topic").name()).isEqualTo("org.neo4j.ogm.domain.forum.Topic");
    }

    /**
     * A class can be found via its annotated label
     */
    @Test
    public void testAnnotatedClassInfo() {
        assertThat(metaData.classInfo("User").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.classInfo("Bronze").name()).isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
    }

    @Test
    public void testCanResolveRelationshipEntityFromRelationshipType() {
        ClassInfo classInfo = metaData.resolve("MEMBER_OF");
        assertThat(classInfo).as("The resolved class info shouldn't be null").isNotNull();
        assertThat(classInfo.name()).isEqualTo("org.neo4j.ogm.domain.canonical.ArbitraryRelationshipEntity");
    }

    @Test
    public void testCanResolveClassHierarchies() {
        ClassInfo classInfo = metaData.resolve("Login", "User");
        assertThat(classInfo.name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
    }

    @Test // GH-391
    public void shouldLookupClassesByTheirSimpleNameCorrectly() {

        MetaData metaData1 = new MetaData("org.neo4j.ogm.domain.gh551", "org.neo4j.ogm.domain.gh391");

        assertThat(metaData1.classInfo(SomeContainer.StaticInnerThingResult.class.getSimpleName())).isNotNull();
        assertThat(metaData1.classInfo("I dont't exist")).isNull();
        assertThat(metaData1.classInfo(".*")).isNull();
        assertThat(metaData1.classInfo(ThingResult.class.getName())).isNotNull();
        assertThat(metaData1.classInfo(ThingResult.class.getSimpleName())).isNotNull();
        assertThat(metaData1.classInfo(ClassWithNonUniqueSimpleName.class.getName())).isNotNull();
        assertThat(metaData1.classInfo(SomeContainer.ClassWithNonUniqueSimpleName.class.getName())).isNotNull();
        assertThatExceptionOfType(MappingException.class)
            .isThrownBy(() -> metaData1.classInfo("ClassWithNonUniqueSimpleName"));
    }

    @Test(expected = AmbiguousBaseClassException.class)
    public void testCannotResolveInconsistentClassHierarchies() {
        metaData.resolve("Login", "Topic");
    }

    /**
     * Taxa corresponding to interfaces with multiple implementations can't be resolved
     */
    @Test
    public void testInterfaceWithMultipleImplTaxa() {
        assertThat(metaData.resolve("IMembership")).isEqualTo(null);
    }

    /**
     * Taxa corresponding to interfaces with a single implementor can be resolved
     * DATAGRAPH-577
     */
    @Test
    public void testInterfaceWithSingleImplTaxa() {
        ClassInfo classInfo = metaData.resolve("AnnotatedInterfaceWithSingleImpl");
        assertThat(classInfo).isNotNull();
        assertThat(classInfo.name())
            .isEqualTo("org.neo4j.ogm.domain.hierarchy.domain.annotated.AnnotatedChildWithAnnotatedInterface");
    }

    /**
     * Taxa corresponding to abstract classes can't be resolved
     */
    @Test
    public void testAbstractClassTaxa() {
        assertThat(metaData.resolve("Membership")).isEqualTo(null);
    }

    /**
     * Taxa not forming a class hierarchy cannot be resolved.
     */
    @Test(expected = AmbiguousBaseClassException.class)
    public void testNoCommonLeafInTaxa() {
        metaData.resolve("Topic", "Member");
    }

    /**
     * The ordering of taxa is unimportant.
     */
    @Test
    public void testOrderingOfTaxaIsUnimportant() {
        assertThat(metaData.resolve("Bronze", "Membership", "IMembership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("Bronze", "IMembership", "Membership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("Membership", "IMembership", "Bronze").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("Membership", "Bronze", "IMembership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("IMembership", "Bronze", "Membership").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
        assertThat(metaData.resolve("IMembership", "Membership", "Bronze").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.BronzeMembership");
    }

    @Test // DATAGRAPH-634
    public void testLiskovSubstitutionPrinciple() {
        assertThat(metaData.resolve("Member").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.resolve("Login", "Member").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.resolve("Login", "Member").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
        assertThat(metaData.resolve("Member", "Login").name()).isEqualTo("org.neo4j.ogm.domain.forum.Member");
    }

    /**
     * Taxa not in the domain will be ignored.
     */
    @Test
    public void testAllNonMemberTaxa() {
        assertThat(metaData.resolve("Knight", "Baronet")).isNull();
    }

    /**
     * Mixing domain and non-domain taxa is permitted.
     */
    @Test
    public void testNonMemberAndMemberTaxa() {
        assertThat(metaData.resolve("Silver", "Pewter", "Tin").name())
            .isEqualTo("org.neo4j.ogm.domain.forum.SilverMembership");
    }

    @Test // GH-686 and others
    public void containsRootPackageShouldWork() {
        assertThat(MetaData.containsRootPackage(null)).isTrue();
        assertThat(MetaData.containsRootPackage()).isTrue();
        assertThat(MetaData.containsRootPackage("a", "", "foo")).isTrue();
        assertThat(MetaData.containsRootPackage("a", "b")).isFalse();
    }

    /**
     * Ensure that performance does not degrade with a huge number of domain classes.
     */
    @Test // GH-678
    public void performanceSmokeTest() throws Exception {

        // Compile numberOfDomainClasses classes
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);

        String domainPackageName = this.getClass().getPackage().getName() + ".gh678";
        Path tmpDir = Files.createTempDirectory("ogmTest");

        int numberOfDomainClasses = 1_000;
        List<CharSequenceJavaFileObject> files = new ArrayList<>();

        final String defaultDummyNodeEntity = "DummyNodeEntity";
        final String defaultDummyRelationshipEntity = "DummyRelationshipEntity";
        for (int i = 0; i < numberOfDomainClasses; ++i) {
            files.add(new CharSequenceJavaFileObject(domainPackageName + "." + defaultDummyNodeEntity + i,
                NODE_ENTITY_DUMMY_TEMPLATE.replaceAll(defaultDummyNodeEntity, defaultDummyNodeEntity + i)));
            if (i < numberOfDomainClasses - 1) {
                files.add(
                    new CharSequenceJavaFileObject(
                        domainPackageName + "." + defaultDummyRelationshipEntity + i,
                        RELATIONSHIP_ENTITY_DUMMY_TEMPLATE
                            .replaceAll(defaultDummyRelationshipEntity, defaultDummyRelationshipEntity + i)
                            .replaceAll("START", defaultDummyNodeEntity + i)
                            .replaceAll("END", defaultDummyNodeEntity + (i + 1))
                    )
                );
            }
        }
        compiler.getTask(null, manager, null, Arrays.asList("-d", tmpDir.toString()), null, files).call();

        // Add the generated classes to our class loader
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        try {
            URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { tmpDir.toUri().toURL() },
                currentThread.getContextClassLoader());
            currentThread.setContextClassLoader(urlClassLoader);

            long start = System.currentTimeMillis();
            MetaData metaData = new MetaData(domainPackageName);
            long duration = (System.currentTimeMillis() - start);
            LOGGER.warn("Scanning took {}ms", duration);

            for(int i = numberOfDomainClasses-2; i>=0; --i) {
                start = System.currentTimeMillis();
                ClassInfo classInfo = metaData.classInfo("DummyNodeEntity" + i);
                duration = (System.currentTimeMillis() - start);
                LOGGER.info("Retrieval of class info for {} took {}ms", classInfo.getUnderlyingClass().getSimpleName(), duration);

                start = System.currentTimeMillis();
                classInfo = metaData.classInfo("DummyRelationshipEntity" + i);
                duration = (System.currentTimeMillis() - start);
                LOGGER.info("Retrieval of class info for {} took {}ms", classInfo.getUnderlyingClass().getSimpleName(), duration);
            }
        } finally {
            // Remove the modified class loader from the current thread
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    private static final class CharSequenceJavaFileObject
        extends SimpleJavaFileObject {
        final CharSequence content;

        public CharSequenceJavaFileObject(
            String className,
            CharSequence content
        ) {
            super(URI.create(
                "string:///"
                    + className.replace('.', '/')
                    + JavaFileObject.Kind.SOURCE.extension),
                JavaFileObject.Kind.SOURCE);
            this.content = content;
        }

        @Override
        public CharSequence getCharContent(
            boolean ignoreEncodingErrors
        ) {
            return content;
        }
    }

    private final static String NODE_ENTITY_DUMMY_TEMPLATE = ""
        + "package org.neo4j.ogm.metadata.gh678;\n\n"
        + "import org.neo4j.ogm.annotation.GeneratedValue;\n"
        + "import org.neo4j.ogm.annotation.Id;\n"
        + "import org.neo4j.ogm.annotation.NodeEntity;\n"
        + "import org.neo4j.ogm.annotation.Property;\n"
        + "\n"
        + "@NodeEntity\n"
        + "public class DummyNodeEntity {\n"
        + "\n"
        + "    @Id @GeneratedValue\n"
        + "    private Long id;\n"
        + "\n"
        + "    @Property(name = \"weirdo\")\n"
        + "    private String a;\n"
        + "\n"
        + "    private String b;\n"
        + "\n"
        + "    private Double n;\n"
        + "\n"
        + "    private Long l;\n"
        + "\n"
        + "    public Long getId() {\n"
        + "        return id;\n"
        + "    }\n"
        + "\n"
        + "    public void setId(Long id) {\n"
        + "        this.id = id;\n"
        + "    }\n"
        + "\n"
        + "    public String getA() {\n"
        + "        return a;\n"
        + "    }\n"
        + "\n"
        + "    public void setA(String a) {\n"
        + "        this.a = a;\n"
        + "    }\n"
        + "\n"
        + "    public String getB() {\n"
        + "        return b;\n"
        + "    }\n"
        + "\n"
        + "    public void setB(String b) {\n"
        + "        this.b = b;\n"
        + "    }\n"
        + "\n"
        + "    public Double getN() {\n"
        + "        return n;\n"
        + "    }\n"
        + "\n"
        + "    public void setN(Double n) {\n"
        + "        this.n = n;\n"
        + "    }\n"
        + "\n"
        + "    public Long getL() {\n"
        + "        return l;\n"
        + "    }\n"
        + "\n"
        + "    public void setL(Long l) {\n"
        + "        this.l = l;\n"
        + "    }\n"
        + "}\n";

    private static final String RELATIONSHIP_ENTITY_DUMMY_TEMPLATE = ""
        + "package org.neo4j.ogm.metadata.gh678;\n\n"
        + "import org.neo4j.ogm.annotation.RelationshipEntity;\n"
        + "import org.neo4j.ogm.annotation.EndNode;\n"
        + "import org.neo4j.ogm.annotation.StartNode;\n"
        + "\n"
        + "@RelationshipEntity\n"
        + "public class DummyRelationshipEntity {\n"
        + "    @StartNode\n"
        + "    private START startNode;\n"
        + "    \n"
        + "    @EndNode\n"
        + "    private END endNode;"
        + "};";
}

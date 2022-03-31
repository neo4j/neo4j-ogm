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
package org.neo4j.ogm.autoindex;

import static java.util.Collections.*;
import static org.neo4j.ogm.transaction.Transaction.Type.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.neo4j.ogm.annotation.CompositeIndex;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.DefaultRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controls the deletion and creation of indexes in the OGM.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 * @author Michael J. Simons
 * @author Gerrit Meier
 * @deprecated The usage of this tool is deprecated. Please use a proper migration tooling, like neo4j-migrations or liquibase with the Neo4j plugin.
 */
@Deprecated
public class AutoIndexManager {

    /**
     * This pattern is used to detect composite keys on attributes that are converted to a map.
     */
    public static final Pattern COMPOSITE_KEY_MAP_COMPOSITE_PATTERN = Pattern.compile("(.+)\\.(.+)");

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoIndexManager.class);

    private final AutoIndexMode mode;
    private final String dumpDir;
    private final String dumpFilename;

    private final Set<AutoIndex> indexes;

    private final Neo4jSession session;
    private DatabaseInformation databaseInfo;

    public AutoIndexManager(MetaData metaData, Configuration configuration, Neo4jSession session) {

        this.mode = configuration.getAutoIndex();
        this.dumpDir = configuration.getDumpDir();
        this.dumpFilename = configuration.getDumpFilename();
        this.session = session;

        this.indexes = initialiseAutoIndex(metaData);
    }

    /**
     * Runs the auto index manager. Depending on the configured mode it either asserts, updates, validates or dumps
     * indexes. Does nothing in all other cases.
     */
    public void run() {

        databaseInfo = DatabaseInformation.parse(session.query("call dbms.components()",
            emptyMap()).queryResults().iterator().next());

        switch (this.mode) {
            case ASSERT:
                assertIndexes();
                break;

            case UPDATE:
                updateIndexes();
                break;

            case VALIDATE:
                validateIndexes();
                break;

            case DUMP:
                dumpIndexes();
                break;

            default:
        }
    }

    private void dumpIndexes() {

        List<String> dumpContent = new ArrayList<>();
        for (AutoIndex index : indexes) {
            dumpContent.add(index.getCreateStatement().getStatement());
        }

        Path dumpPath = Paths.get(this.dumpDir, this.dumpFilename);
        LOGGER.debug("Dumping Indexes to: [{}]", dumpPath.toAbsolutePath());

        try {
            Files.write(dumpPath, dumpContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not write file to " + dumpPath.toAbsolutePath(), e);
        }
    }

    private void validateIndexes() {

        LOGGER.debug("Validating indexes and constraints");

        List<AutoIndex> copyOfIndexes = new ArrayList<>(indexes);
        List<AutoIndex> dbIndexes = loadIndexesFromDB();
        copyOfIndexes.removeAll(dbIndexes);

        if (!copyOfIndexes.isEmpty()) {

            StringBuilder missingIndexes = new StringBuilder("[");

            for (AutoIndex s : copyOfIndexes) {
                missingIndexes.append(s.getDescription()).append(", ");
            }
            missingIndexes.append("]");
            throw new MissingIndexException(
                "Validation of Constraints and Indexes failed. Could not find the following : " + missingIndexes);
        }
    }

    private void assertIndexes() {

        LOGGER.debug("Asserting indexes and constraints");

        List<Statement> dropStatements = new ArrayList<>();

        List<AutoIndex> dbIndexes = loadIndexesFromDB();

        for (AutoIndex dbIndex : dbIndexes) {
            LOGGER.debug("[{}] added to drop statements.", dbIndex.getDescription());
            dropStatements.add(dbIndex.getDropStatement());

        }

        DefaultRequest dropIndexesRequest = new DefaultRequest();
        dropIndexesRequest.setStatements(dropStatements);
        LOGGER.debug("Dropping all indexes and constraints");

        // make sure drop and create happen in separate transactions
        // neo does not support that
        session.doInTransaction(() -> {
            session.requestHandler().execute(dropIndexesRequest);
        }, READ_WRITE);

        create();
    }

    private List<AutoIndex> loadIndexesFromDB() {
        List<AutoIndex> dbIndexes = new ArrayList<>();

        session.doInTransaction(() -> {
            Result query = session.query("SHOW CONSTRAINTS YIELD *", emptyMap());
            for (Map<String, Object> queryResult : query.queryResults()) {
                Optional<AutoIndex> dbIndex = AutoIndex.parseConstraint(queryResult);
                dbIndex.ifPresent(dbIndexes::add);
            }
        }, READ_ONLY);

        session.doInTransaction(() -> {
            Result query = session.query("SHOW INDEXES YIELD *", emptyMap());
            for (Map<String, Object> queryResult : query.queryResults()) {
                Optional<AutoIndex> dbIndex = AutoIndex.parseIndex(queryResult);
                dbIndex.ifPresent(dbIndexes::add);
            }
        }, READ_ONLY);

        return dbIndexes;
    }

    private void updateIndexes() {
        LOGGER.info("Updating indexes and constraints");

        List<Statement> dropStatements = new ArrayList<>();
        List<AutoIndex> dbIndexes = loadIndexesFromDB();
        for (AutoIndex dbIndex : dbIndexes) {
            if (dbIndex.hasOpposite() && indexes.contains(dbIndex.createOppositeIndex())) {
                dropStatements.add(dbIndex.getDropStatement());
            }
        }
        executeStatements(dropStatements);

        List<Statement> createStatements = new ArrayList<>();
        for (AutoIndex index : indexes) {
            if (!dbIndexes.contains(index)) {
                createStatements.add(index.getCreateStatement());
            }
        }
        executeStatements(createStatements);
    }

    private void executeStatements(List<Statement> statements) {
        DefaultRequest request = new DefaultRequest();
        request.setStatements(statements);

        session.doInTransaction(() -> {
            try (Response<RowModel> response = session.requestHandler().execute(request)) {
                // Success
            }
        }, READ_WRITE);
    }

    private void create() {

        // build indexes according to metadata
        List<Statement> statements = new ArrayList<>();
        for (AutoIndex index : indexes) {
            final Statement createStatement = index.getCreateStatement();
            statements.add(createStatement);

            LOGGER.debug("[{}] added to create statements.", createStatement);
        }

        LOGGER.debug("Creating indexes and constraints.");
        DefaultRequest request = new DefaultRequest();
        request.setStatements(statements);
        session.doInTransaction(() -> session.requestHandler().execute(request).close(), READ_WRITE);
    }

    private static Set<AutoIndex> initialiseAutoIndex(MetaData metaData) {

        LOGGER.debug("Building Index Metadata.");
        Set<AutoIndex> indexMetadata = new HashSet<>();
        for (ClassInfo classInfo : metaData.persistentEntities()) {

            final String owningType = classInfo.neo4jName();

            if (needsToBeIndexed(classInfo)) {

                // We build the composite index first, to find out whether an @Id or @Index annotated field
                // is actually decomposed by a MapCompositeConverter AND has a defined composite index.
                Set<String> decomposedFields = new HashSet<>();
                for (CompositeIndex index : classInfo.getCompositeIndexes()) {
                    IndexType type = index.unique() ? IndexType.NODE_KEY_CONSTRAINT : IndexType.COMPOSITE_INDEX;
                    List<String> properties = new ArrayList<>();
                    Stream.of(index.value().length > 0 ? index.value() : index.properties())
                        .forEach(p -> {
                            Matcher m = COMPOSITE_KEY_MAP_COMPOSITE_PATTERN.matcher(p);
                            if (m.matches()) {
                                decomposedFields.add(m.group(1));
                                properties.add(m.group(2));
                            } else {
                                properties.add(p);
                            }
                        });
                    AutoIndex autoIndex = new AutoIndex(classInfo.getUnderlyingClass(), type, owningType,
                        properties.toArray(new String[0]), null);
                    LOGGER.debug("Adding composite index [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }

                for (FieldInfo fieldInfo : getIndexFields(classInfo)) {

                    IndexType type = fieldInfo.isConstraint() ? IndexType.UNIQUE_CONSTRAINT : IndexType.SINGLE_INDEX;

                    if (fieldInfo.hasCompositeConverter()) {
                        if (!decomposedFields.contains(fieldInfo.getName())) {
                            LOGGER.warn("\n"
                                    + "The field {} of {} should be indexed with an index of type {}.\n"
                                    + "This is not supported on a composite field (a field that is decomposed into a set of properties), no index will be created.\n"
                                    + "Use a @CompositeIndex on the class instead and prefix the properties with `{}.`.",
                                fieldInfo.getName(), classInfo.getUnderlyingClass(), type, fieldInfo.getName());
                        }
                        continue;
                    }

                    final AutoIndex autoIndex = new AutoIndex(classInfo.getUnderlyingClass(), type, owningType, new String[] { fieldInfo.property() }, null);
                    LOGGER.debug("Adding Index [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }
            }

            if (classInfo.hasRequiredFields()) {
                for (FieldInfo requiredField : classInfo.requiredFields()) {
                    IndexType type = classInfo.isRelationshipEntity() ?
                        IndexType.REL_PROP_EXISTENCE_CONSTRAINT : IndexType.NODE_PROP_EXISTENCE_CONSTRAINT;

                    AutoIndex autoIndex = new AutoIndex(classInfo.getUnderlyingClass(), type, owningType,
                        new String[] { requiredField.property() }, null);

                    LOGGER.debug("Adding required constraint [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }
            }
        }
        return indexMetadata;
    }

    /**
     * This methods checks whether a class described by <code>classInfo</code> needs be taken into consideration for
     * indexes, either directly or in an inheritance hierarchy.<br>
     * A class needs to be indexed when its not abstract and  itself or one of its superclasses contains indexes.
     *
     * @param classInfo {@link ClassInfo} describing the class possible contributing to an index.
     * @return True, if the the class in question contributes to an index.
     */
    private static boolean needsToBeIndexed(ClassInfo classInfo) {
        return (!classInfo.isAbstract() || classInfo.neo4jName() != null) && containsIndexesInHierarchy(classInfo);
    }

    /**
     * Checks <code>classInfo</code> if there's any direct superclass that contains indexes. Stops at the end
     * of the hierarchy or at the first occurrence of indexes.
     *
     * @param classInfo The {@link ClassInfo} providing the hierarchy to check, also checked for indexes itself. A null
     *                  parameter ends recursion.
     * @return True, if any direct superclass contains indexes.
     */
    private static boolean containsIndexesInHierarchy(ClassInfo classInfo) {

        boolean containsIndexes = false;
        ClassInfo currentClassInfo = classInfo;
        while (!containsIndexes && currentClassInfo != null) {
            containsIndexes = currentClassInfo.containsIndexes();
            currentClassInfo = currentClassInfo.directSuperclass();
        }
        return containsIndexes;
    }

    /**
     * Computes all fields that needs to be indexed. Includes all the fields of the classes in the hierarchy if the
     * class in question is not to be indexed anyway.
     *
     * @param classInfo {@link ClassInfo} representing the end point of a hierarchy for which indexes have to be
     *                  collected
     * @return All fields contributing to indexes in the hierarchy of the class described by <code>classInfo</code>
     */
    private static List<FieldInfo> getIndexFields(ClassInfo classInfo) {

        List<FieldInfo> indexFields = new ArrayList<>();
        ClassInfo currentClassInfo = classInfo.directSuperclass();
        while (currentClassInfo != null) {
            if (!needsToBeIndexed(currentClassInfo)) {
                indexFields.addAll(currentClassInfo.getIndexFields());
            }
            currentClassInfo = currentClassInfo.directSuperclass();
        }

        indexFields.addAll(classInfo.getIndexFields());
        return indexFields;
    }

    private static class DatabaseInformation {

        final String version;
        final String edition;

        private DatabaseInformation(String version, String edition) {
            this.version = version;
            this.edition = edition;
        }

        static DatabaseInformation parse(Map<String, Object> databaseInformation) {

            return new DatabaseInformation(extractVersion(databaseInformation),
                (String) databaseInformation.get("edition"));
        }

        private static String extractVersion(Map<String, Object> databaseInformation) {
            return ((String[]) databaseInformation.get("versions"))[0];
        }
    }
}

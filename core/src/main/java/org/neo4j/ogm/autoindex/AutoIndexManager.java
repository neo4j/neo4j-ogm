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
package org.neo4j.ogm.autoindex;

import static java.util.Collections.*;
import static org.neo4j.ogm.transaction.Transaction.Type.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.neo4j.ogm.annotation.CompositeIndex;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.request.DefaultRequest;
import org.neo4j.ogm.session.request.RowDataStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controls the deletion and creation of indexes in the OGM.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 * @author Michael J. Simons
 */
public class AutoIndexManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoIndexManager.class);

    private final AutoIndexMode mode;
    private final String dumpDir;
    private final String dumpFilename;

    private final List<AutoIndex> indexes;

    private final Neo4jSession session;

    public AutoIndexManager(MetaData metaData, Configuration configuration, Neo4jSession session) {

        this.mode = configuration.getAutoIndex();
        this.dumpDir = configuration.getDumpDir();
        this.dumpFilename = configuration.getDumpFilename();
        this.session = session;

        this.indexes = initialiseAutoIndex(metaData);
    }

    /**
     * Builds indexes according to the configured mode.
     * @deprecated since 3.1.3, use {@link #run()}  instead.
     */
    @Deprecated
    public void build() {
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

    /**
     * Runs the auto index manager. Depending on the configured mode it either asserts, updates, validates or dumps
     * indexes. Does nothing in all other cases.
     */
    public void run() {
        this.build();
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

            String missingIndexes = "[";

            for (AutoIndex s : copyOfIndexes) {
                missingIndexes += s.getDescription() + ", ";
            }
            missingIndexes += "]";
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
        DefaultRequest indexRequests = buildProcedures();
        List<AutoIndex> dbIndexes = new ArrayList<>();
        session.doInTransaction(() -> {
            try (Response<RowModel> response = session.requestHandler().execute(indexRequests)) {
                RowModel rowModel;
                while ((rowModel = response.next()) != null) {
                    Optional<AutoIndex> dbIndex = AutoIndex.parse((String) rowModel.getValues()[0]);
                    dbIndex.ifPresent(dbIndexes::add);
                }
            }
        }, READ_WRITE);
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

    private DefaultRequest buildProcedures() {
        List<Statement> procedures = new ArrayList<>();

        procedures.add(new RowDataStatement("CALL db.constraints()", emptyMap()));
        procedures.add(new RowDataStatement("call db.indexes() yield description, type with description, type where type <> 'node_unique_property' return description" , emptyMap()));

        DefaultRequest getIndexesRequest = new DefaultRequest();
        getIndexesRequest.setStatements(procedures);
        return getIndexesRequest;
    }

    private void create() {
        // build indexes according to metadata
        List<Statement> statements = new ArrayList<>();
        for (AutoIndex index : indexes) {
            final Statement createStatement = index.getCreateStatement();
            LOGGER.debug("[{}] added to create statements.", createStatement);
            statements.add(createStatement);
        }
        DefaultRequest request = new DefaultRequest();
        request.setStatements(statements);
        LOGGER.debug("Creating indexes and constraints.");

        session.doInTransaction(() -> {
            try (Response<RowModel> response = session.requestHandler().execute(request)) {
                // Success
            }
        }, READ_WRITE);
    }

    private static List<AutoIndex> initialiseAutoIndex(MetaData metaData) {

        LOGGER.debug("Building Index Metadata.");
        List<AutoIndex> indexMetadata = new ArrayList<>();
        for (ClassInfo classInfo : metaData.persistentEntities()) {

            final String owningType = classInfo.neo4jName();

            if (needsToBeIndexed(classInfo)) {
                for (FieldInfo fieldInfo : getIndexFields(classInfo)) {
                    IndexType type = fieldInfo.isConstraint() ? IndexType.UNIQUE_CONSTRAINT : IndexType.SINGLE_INDEX;
                    final AutoIndex autoIndex = new AutoIndex(type, owningType,
                        new String[] { fieldInfo.property() });
                    LOGGER.debug("Adding Index [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }

                for (CompositeIndex index : classInfo.getCompositeIndexes()) {
                    IndexType type = index.unique() ? IndexType.NODE_KEY_CONSTRAINT : IndexType.COMPOSITE_INDEX;
                    String[] properties = index.value().length > 0 ? index.value() : index.properties();
                    AutoIndex autoIndex = new AutoIndex(type, owningType, properties);
                    LOGGER.debug("Adding composite index [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }
            }

            if (classInfo.hasRequiredFields()) {
                for (FieldInfo requiredField : classInfo.requiredFields()) {
                    IndexType type = classInfo.isRelationshipEntity() ?
                        IndexType.REL_PROP_EXISTENCE_CONSTRAINT : IndexType.NODE_PROP_EXISTENCE_CONSTRAINT;

                    AutoIndex autoIndex = new AutoIndex(type, owningType,
                        new String[] { requiredField.property() });

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
}

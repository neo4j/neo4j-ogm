/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
package org.neo4j.ogm.autoindex;

import org.neo4j.ogm.annotation.CompositeIndex;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.request.DefaultRequest;
import org.neo4j.ogm.session.request.RowDataStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static org.neo4j.ogm.config.AutoIndexMode.NONE;
import static org.neo4j.ogm.transaction.Transaction.Type.READ_WRITE;

/**
 * This class controls the deletion and creation of indexes in the OGM.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 */
public class AutoIndexManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfo.class);

    private List<AutoIndex> indexes;
    private Neo4jSession session;
    private Configuration configuration;

    public AutoIndexManager(MetaData metaData, Configuration configuration, SessionFactory sessionFactory) {
        if (configuration.getAutoIndex() != NONE) {
            this.configuration = configuration;
            this.indexes = initialiseIndexMetadata(metaData);
            this.session = (Neo4jSession) sessionFactory.openSession();
            build();
        }
    }

    private List<AutoIndex> initialiseIndexMetadata(MetaData metaData) {
        LOGGER.debug("Building Index Metadata.");
        List<AutoIndex> indexMetadata = new ArrayList<>();
        for (ClassInfo classInfo : metaData.persistentEntities()) {

            if (classInfo.containsIndexes()) {
                for (FieldInfo fieldInfo : classInfo.getIndexFields()) {
                    IndexType type = fieldInfo.isConstraint() ? IndexType.UNIQUE_CONSTRAINT : IndexType.SINGLE_INDEX;
                    final AutoIndex autoIndex = new AutoIndex(type, classInfo.neo4jName(),
                        new String[]{fieldInfo.property()});
                    LOGGER.debug("Adding Index [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }

                for (CompositeIndex index : classInfo.getCompositeIndexes()) {
                    IndexType type = index.unique() ? IndexType.NODE_KEY_CONSTRAINT : IndexType.COMPOSITE_INDEX;
                    String[] properties = index.value().length > 0 ? index.value() : index.properties();
                    AutoIndex autoIndex = new AutoIndex(type, classInfo.neo4jName(), properties);
                    LOGGER.debug("Adding composite index [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }
            }

            if (classInfo.hasRequiredFields()) {
                for (FieldInfo requiredField : classInfo.requiredFields()) {
                    IndexType type = classInfo.isRelationshipEntity() ?
                        IndexType.REL_PROP_EXISTENCE_CONSTRAINT : IndexType.NODE_PROP_EXISTENCE_CONSTRAINT;

                    AutoIndex autoIndex = new AutoIndex(type, classInfo.neo4jName(),
                        new String[]{requiredField.property()});

                    LOGGER.debug("Adding required constraint [description={}]", autoIndex);
                    indexMetadata.add(autoIndex);
                }
            }
        }
        return indexMetadata;
    }

    List<AutoIndex> getIndexes() {
        return indexes;
    }

    /**
     * Builds indexes according to the configured mode.
     */
    private void build() {
        switch (configuration.getAutoIndex()) {
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
            default:
        }
    }

    private void dumpIndexes() {
        final String newLine = System.lineSeparator();

        StringBuilder sb = new StringBuilder();
        for (AutoIndex index : indexes) {
            sb.append(index.getCreateStatement().getStatement()).append(newLine);
        }

        File file = new File(configuration.getDumpDir(), configuration.getDumpFilename());
        FileWriter writer = null;

        LOGGER.debug("Dumping Indexes to: [{}]", file.toString());

        try {
            writer = new FileWriter(file);
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Could not write file to " + file.getAbsolutePath(), e);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException ignore) {
                }
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

                    // Ignore index descriptions for constraints
                    // neo4j up to 3.3 returns 3 columns, type in column number 2
                    // neo4j 3.4 returns 6 columns, type in column number 4
                    if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property") ||
                        rowModel.getValues().length == 6 && rowModel.getValues()[4].equals("node_unique_property")) {

                        continue;
                    }

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
        procedures.add(new RowDataStatement("CALL db.indexes()", emptyMap()));

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

}

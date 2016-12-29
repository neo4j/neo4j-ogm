/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.request.DefaultRequest;
import org.neo4j.ogm.session.request.RowDataStatement;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controls the deletion and creation of indexes in the OGM.
 *
 * @author Mark Angrish
 */
public class AutoIndexManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfo.class);

    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    private final List<AutoIndex> indexes;

    private final AutoIndexMode mode;

    private final Driver driver;

    public AutoIndexManager(MetaData metaData, Driver driver) {

        this.driver = initialiseDriver(driver);
        this.mode = Components.autoIndexMode();
        this.indexes = initialiseIndexMetadata(metaData);
    }

    private Driver initialiseDriver(Driver driver) {
        driver.setTransactionManager(new DefaultTransactionManager(null, driver));
        return driver;
    }

    private List<AutoIndex> initialiseIndexMetadata(MetaData metaData) {
        LOGGER.debug("Building Index Metadata.");
        List<AutoIndex> indexMetadata = new ArrayList<>();
        for (ClassInfo classInfo : metaData.persistentEntities()) {

            if (classInfo.containsIndexes()) {
                for (FieldInfo fieldInfo : classInfo.getIndexFields()) {
                    final AutoIndex index = new AutoIndex(classInfo.neo4jName(), fieldInfo.property(), fieldInfo.isConstraint());
                    LOGGER.debug("Adding Index [description={}]", index);
                    indexMetadata.add(index);
                }
            }
        }
        return indexMetadata;
    }

    public List<AutoIndex> getIndexes() {
        return indexes;
    }

    /**
     * Builds indexes according to the configured mode.
     */
    public void build() {
        switch (mode) {
            case ASSERT:
                assertIndexes();
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

        File file = new File(Components.getConfiguration().autoIndexConfiguration().getDumpDir(),
                Components.getConfiguration().autoIndexConfiguration().getDumpFilename());
        FileWriter writer = null;

        LOGGER.debug("Dumping Indexes to: [{}]", file.toString());

        try {
            writer = new FileWriter(file);
            writer.write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException("Could not write file to " + file.getAbsolutePath());
        } finally {
            if (writer != null) try {
                writer.close();
            } catch (IOException ignore) {
            }
        }
    }

    private void validateIndexes() {

        LOGGER.debug("Validating Indexes");

        DefaultRequest indexRequests = buildProcedures();
        List<AutoIndex> copyOfIndexes = new ArrayList<>(indexes);

        try (Response<RowModel> response = driver.request().execute(indexRequests)) {
            RowModel rowModel;
            while ((rowModel = response.next()) != null) {
                if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property")) {
                    continue;
                }
                for (AutoIndex index : indexes) {
                    if (index.getDescription().replaceAll("\\s+", "").equalsIgnoreCase(((String) rowModel.getValues()[0]).replaceAll("\\s+", ""))) {
                        copyOfIndexes.remove(index);
                    }
                }
            }
        }

        if (!copyOfIndexes.isEmpty()) {

            String missingIndexes = "[";

            for (AutoIndex s : copyOfIndexes) {
                missingIndexes += s.getDescription() + ", ";
            }
            missingIndexes += "]";
            throw new MissingIndexException("Validation of Constraints and Indexes failed. Could not find the following : " + missingIndexes);
        }
    }

    private void assertIndexes() {

        LOGGER.debug("Asserting Indexes.");

        DefaultRequest indexRequests = buildProcedures();
        List<Statement> dropStatements = new ArrayList<>();

        try (Response<RowModel> response = driver.request().execute(indexRequests)) {
            RowModel rowModel;
            while ((rowModel = response.next()) != null) {
                if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property")) {
                    continue;
                }
                // can replace this with a lookup of the Index by description but attaching DROP here is faster.
                final String dropStatement = "DROP " + rowModel.getValues()[0];
                LOGGER.debug("[{}] added to drop statements.", dropStatement);
                dropStatements.add(new RowDataStatement(dropStatement, EMPTY_MAP));
            }
        }

        DefaultRequest dropIndexesRequest = new DefaultRequest();
        dropIndexesRequest.setStatements(dropStatements);
        LOGGER.debug("Dropping all indexes and constraints");

        try (Response<RowModel> response = driver.request().execute(dropIndexesRequest)) {
        }

        create();
    }

    private DefaultRequest buildProcedures() {
        if (Components.neo4jVersion() < 3.0) {
            throw new Neo4jVersionException("This configuration of auto indexing requires Neo4j version 3.0 or higher.");
        }
        List<Statement> procedures = new ArrayList<>();

        procedures.add(new RowDataStatement("CALL db.constraints()", EMPTY_MAP));
        procedures.add(new RowDataStatement("CALL db.indexes()", EMPTY_MAP));

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

        try (Response<RowModel> response = driver.request().execute(request)) {
            // Success
        }
    }
}

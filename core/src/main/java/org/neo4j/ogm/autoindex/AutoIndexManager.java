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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class controls the deletion and creation of indexes in the OGM.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 */
public class AutoIndexManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInfo.class);

    private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

    private final List<AutoIndex> indexes;
    private Neo4jSession session;

    private final Configuration configuration;

    public AutoIndexManager(MetaData metaData, Configuration configuration, Neo4jSession session) {

        this.configuration = configuration;
        this.indexes = initialiseIndexMetadata(metaData);
        this.session = session;
    }

    private List<AutoIndex> initialiseIndexMetadata(MetaData metaData) {
        LOGGER.debug("Building Index Metadata.");
        List<AutoIndex> indexMetadata = new ArrayList<>();
        for (ClassInfo classInfo : metaData.persistentEntities()) {

            if (classInfo.containsIndexes()) {
                for (FieldInfo fieldInfo : classInfo.getIndexFields()) {
                    final AutoIndex index = new AutoIndex(classInfo.neo4jName(), fieldInfo.property(),
                        fieldInfo.isConstraint());
                    LOGGER.debug("Adding Index [description={}]", index);
                    indexMetadata.add(index);
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
    public void build() {
        switch (configuration.getAutoIndex()) {
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

        LOGGER.debug("Validating Indexes");

        DefaultRequest indexRequests = buildProcedures();
        List<AutoIndex> copyOfIndexes = new ArrayList<>(indexes);

        try (Transaction tx = session.beginTransaction()) {
            try (Response<RowModel> response = session.requestHandler().execute(indexRequests)) {
                RowModel rowModel;
                while ((rowModel = response.next()) != null) {
                    if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property")) {
                        continue;
                    }
                    for (AutoIndex index : indexes) {
                        String description = index.getDescription();

                        // The rowModel values below, as returned from the request to Neo4j, do not contain escape characters
                        // Therefore remove escape characters from the description so as to correctly match the rowModel values
                        description = description.replace("`", "");

                        if (description.replaceAll("\\s+", "")
                            .equalsIgnoreCase(((String) rowModel.getValues()[0]).replaceAll("\\s+", ""))) {
                            copyOfIndexes.remove(index);
                        }
                    }
                }
            }
            tx.commit();
        }

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

        LOGGER.debug("Asserting Indexes.");

        DefaultRequest indexRequests = buildProcedures();
        List<Statement> dropStatements = new ArrayList<>();

        try (Transaction tx = session.beginTransaction()) {try (Response<RowModel> response = session.requestHandler().execute(indexRequests)) {
            RowModel rowModel;
            while ((rowModel = response.next()) != null) {
                // Ignore index descriptions for constraints
                // neo4j up to 3.3 returns 3 columns, type in column number 2
                // neo4j 3.4 returns 6 columns, type in column number 4
                if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property")||
                    rowModel.getValues().length == 6 && rowModel.getValues()[4].equals("node_unique_property")) {
                    continue;
                }
                // can replace this with a lookup of the Index by description but attaching DROP here is faster.
                String statement = (String) rowModel.getValues()[0];

                    // The statement is provided by the response from Neo4j and may not be property escaped for execution
                    if (statement.startsWith("CONSTRAINT")) {
                        statement = escapeConstraintStatement(statement);
                    } else if (statement.startsWith("INDEX")) {
                        statement = escapeIndexStatement(statement);
                    }

                    final String dropStatement = "DROP " + statement;

                    LOGGER.debug("[{}] added to drop statements.", dropStatement);
                    dropStatements.add(new RowDataStatement(dropStatement, EMPTY_MAP));
                }
            }
            tx.commit();
        }

        DefaultRequest dropIndexesRequest = new DefaultRequest();
        dropIndexesRequest.setStatements(dropStatements);
        LOGGER.debug("Dropping all indexes and constraints");

        // make sure drop and create happen in separate transactions
        // neo does not support that
        try (Transaction tx = session.beginTransaction()) {
            session.requestHandler().execute(dropIndexesRequest);
            tx.commit();
        }

        create();
    }

    private DefaultRequest buildProcedures() {
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

        try (Transaction tx = session.beginTransaction()) {
            try (Response<RowModel> response = session.requestHandler().execute(request)) {
                // Success
            }
            tx.commit();
        }
    }

    /**
     * Perform String manipulations to transform the incoming constraint statement to be property escaped.
     *
     * @param statement A constraint statement, possibly unescaped.
     * @return A properly escaped constraint statement.
     */
    private String escapeConstraintStatement(String statement) {

        int startIndex = statement.indexOf("CONSTRAINT ON (");

        if (startIndex != -1) {

            StringBuilder str = new StringBuilder(statement);

            startIndex = startIndex + 16;
            str = str.insert(startIndex, "`");

            startIndex = str.indexOf(":", startIndex);
            str = str.insert(startIndex, "`");

            startIndex = startIndex + 2;
            str = str.insert(startIndex, "`");

            startIndex = str.indexOf(" ", startIndex);
            str = str.insert(startIndex, "`");

            startIndex = str.indexOf("ASSERT ", startIndex);
            startIndex = startIndex + 7;
            str = str.insert(startIndex, "`");

            startIndex = str.indexOf(".", startIndex);
            str = str.insert(startIndex, "`");

            startIndex = startIndex + 2;
            str = str.insert(startIndex, "`");

            startIndex = str.indexOf(" ", startIndex);
            str = str.insert(startIndex, "`");

            statement = str.toString();
        }

        return statement;

    }

    /**
     * Perform String manipulations to transform the incoming index statement to be property escaped.
     *
     * @param statement A index statement, possibly unescaped.
     * @return A properly escaped index statement.
     */
    private String escapeIndexStatement(String statement) {

        int startIndex = statement.indexOf("INDEX ON :");

        if (startIndex != -1) {

            StringBuilder str = new StringBuilder(statement);

            startIndex = startIndex + 10;
            str = str.insert(startIndex, "`");

            startIndex = str.indexOf("(", startIndex);
            str = str.insert(startIndex, "`");

            startIndex = startIndex + 2;
            str = str.insert(startIndex, "`");

            startIndex = str.indexOf(")", startIndex);
            str = str.insert(startIndex, "`");

            statement = str.toString();
        }

        return statement;

    }
}

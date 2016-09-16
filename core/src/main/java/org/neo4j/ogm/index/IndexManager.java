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
package org.neo4j.ogm.index;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.request.DefaultRequest;
import org.neo4j.ogm.session.request.RowDataStatement;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;

/**
 * This class controls the deletion and creation of indexes in the OGM.
 *
 * @author Mark Angrish
 */
public class IndexManager {

	private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();
	private static final String DEFAULT_GENERATED_INDEXES_FILENAME = "generated_indexes.cql";

	private List<Index> indexes;

	private AutoIndexMode mode;

	private Driver driver;

	public IndexManager(MetaData metaData, Driver driver) {

		this.driver = initialiseDriver(driver);
		this.mode = Components.autoIndexMode();
		this.indexes = initialiseIndexMetadata(metaData);
	}

	private Driver initialiseDriver(Driver driver) {
		driver.setTransactionManager(new DefaultTransactionManager(null, driver));
		return driver;
	}

	private List<Index> initialiseIndexMetadata(MetaData metaData) {
		List<Index> indexMetadata = new ArrayList<>();
		for (ClassInfo classInfo : metaData.persistentEntities()) {

			if (classInfo.containsIndexes()) {
				for (FieldInfo fieldInfo : classInfo.getIndexFields()) {
					indexMetadata.add(new Index(classInfo.neo4jName(), fieldInfo.property(), fieldInfo.isConstraint()));
				}
			}
		}
		return indexMetadata;
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
		for (Index index : indexes) {
			sb.append(index.getCreateStatement().getStatement()).append(newLine);
		}

		// Generate indexes in file at root directory.
		// TODO: should this be configurable too?
		File file = new File(DEFAULT_GENERATED_INDEXES_FILENAME);
		FileWriter writer = null;

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
		DefaultRequest getIndexesRequest = buildProcedures();
		List<Index> copyOfIndexes = new ArrayList<>(indexes);

		try (Response<RowModel> response = driver.request().execute(getIndexesRequest)) {
			RowModel rowModel;
			while ((rowModel = response.next()) != null) {
				if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property")) {
					continue;
				}
				for (Index index : indexes) {
					if (index.getDescription().equals(rowModel.getValues()[0])) {
						copyOfIndexes.remove(index);
					}
				}
			}
		}

		if (!copyOfIndexes.isEmpty()) {

			String missingIndexes = "[";

			for (Index s : copyOfIndexes) {
				missingIndexes += s.getDescription() + ", ";
			}
			missingIndexes += "]";
			throw new RuntimeException("Validation of Constraints and IndexManager failed. Could not find the following : " + missingIndexes);
		}
	}

	private void assertIndexes() {
		DefaultRequest getIndexesRequest = buildProcedures();
		List<Statement> dropStatements = new ArrayList<>();

		try (Response<RowModel> response = driver.request().execute(getIndexesRequest)) {
			RowModel rowModel;
			while ((rowModel = response.next()) != null) {
				if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property")) {
					continue;
				}
				// can replace this with a lookup of the Index by description but attaching DROP here is faster.
				dropStatements.add(new RowDataStatement("DROP " + rowModel.getValues()[0], EMPTY_MAP));
			}
		}

		DefaultRequest dropIndexesRequest = new DefaultRequest();
		dropIndexesRequest.setStatements(dropStatements);
		try (Response<RowModel> response = driver.request().execute(dropIndexesRequest)) {
		}

		create();
	}

	private DefaultRequest buildProcedures() {
		if (Components.neo4jVersion() < 3.0) {
			throw new RuntimeException("This configuration of auto indexing requires Neo4j version 3.0 or higher.");
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
		for (Index index : indexes) {
			statements.add(index.getCreateStatement());
		}
		DefaultRequest request = new DefaultRequest();
		request.setStatements(statements);
		try (Response<RowModel> response = driver.request().execute(request)) {
			// Success
		}
	}
}

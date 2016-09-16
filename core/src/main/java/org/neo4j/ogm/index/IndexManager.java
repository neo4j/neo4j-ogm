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
 */
public class IndexManager {

	private static final Map<String, Object> EMPTY_MAP = Collections.emptyMap();

	private List<Index> indexes;

	private AutoIndexMode mode;

	private Driver driver;

	public IndexManager(MetaData metaData, Driver driver) {

		this.driver = driver;
		this.driver.setTransactionManager(new DefaultTransactionManager(null, driver));
		this.mode =Components.autoIndexMode();

		this.indexes = new ArrayList<>();
		for (ClassInfo classInfo : metaData.persistentEntities()) {

			if (classInfo.containsIndexes()) {
				for (FieldInfo fieldInfo : classInfo.getIndexFields()) {
					indexes.add(new Index(classInfo.neo4jName(), fieldInfo.property(), fieldInfo.isConstraint()));
				}
			}
		}
	}

	public void build() {
		switch (mode) {
			case CREATE_DROP:
				create();
				break;
			case ASSERT:
				verify();
				break;
			case VALIDATE:
				validate();
				break;
			case DUMP:
				dump();
			default:
		}
	}

	private void dump() {
		// Generate indexes in file at root directory.
		final String newLine = System.lineSeparator();
		StringBuilder sb = new StringBuilder();
		for (Index index : indexes) {
			sb.append(index.getCreateStatement().getStatement()).append(newLine);
		}
		File file = new File("generated_indexes.cql");
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

	private void validate() {
		// confirm all indexes defined in metadata exist on startup or else throw exception.
		if (Components.neo4jVersion() < 3.0) {
			throw new RuntimeException("Auto indexing with value 'assert' requires Neo4j version 3.0 or higher.");
		}
		List<Index> copyOfIndexes = new ArrayList<>(indexes);
		List<Statement> procedures = new ArrayList<>();

		procedures.add(new RowDataStatement("CALL db.constraints()", EMPTY_MAP));
		procedures.add(new RowDataStatement("CALL db.indexes()", EMPTY_MAP));

		DefaultRequest getIndexesRequest = new DefaultRequest();
		getIndexesRequest.setStatements(procedures);
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

	private void verify() {
		// drop all indexes and build from scratch.
		if (Components.neo4jVersion() < 3.0) {
			throw new RuntimeException("Auto indexing with value 'assert' requires Neo4j version 3.0 or higher.");
		}
		List<Statement> procedures = new ArrayList<>();
		List<Statement> dropStatements = new ArrayList<>();

		procedures.add(new RowDataStatement("CALL db.constraints()", EMPTY_MAP));
		procedures.add(new RowDataStatement("CALL db.indexes()", EMPTY_MAP));

		DefaultRequest getIndexesRequest = new DefaultRequest();
		getIndexesRequest.setStatements(procedures);
		try (Response<RowModel> response = driver.request().execute(getIndexesRequest)) {
			RowModel rowModel;
			while ((rowModel = response.next()) != null) {
				if (rowModel.getValues().length == 3 && rowModel.getValues()[2].equals("node_unique_property")) {
					continue;
				}
				dropStatements.add(new RowDataStatement("DROP " + rowModel.getValues()[0], EMPTY_MAP));
			}
		}

		DefaultRequest dropIndexesRequest = new DefaultRequest();
		dropIndexesRequest.setStatements(dropStatements);
		try (Response<RowModel> response = driver.request().execute(dropIndexesRequest)) {
		}

		create();
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

	public void drop() {
		if (mode.equals(AutoIndexMode.CREATE_DROP)) {
			// drop indexes according to metadata
			List<Statement> statements = new ArrayList<>();

			for (Index index : indexes) {
				statements.add(index.getDropStatement());
			}
			DefaultRequest request = new DefaultRequest();
			request.setStatements(statements);
			try (Response<RowModel> response = driver.request().execute(request)) {
				// Success
			}
		}
	}
}

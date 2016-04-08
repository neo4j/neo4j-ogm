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

package org.neo4j.ogm.session.request;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotations.FieldWriter;
import org.neo4j.ogm.compiler.CompileContext;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.context.TransientRelationship;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Plans request execution and processes the response.
 *
 * @author Luanne Misquitta
 */
public class RequestExecutor {

	private Neo4jSession session;

	public RequestExecutor(Neo4jSession session) {
		this.session = session;
	}

	/**
	 * Execute a save request.
	 * Decides how the request is split depending upon characteristics of what is to be saved.
	 * Processes the response(s) and updates the mapping context.
	 *
	 * @param context the CompileContext for this request
	 */
	public void executeSave(CompileContext context) {
		Compiler compiler = context.getCompiler();
		compiler.useStatementFactory(new RowStatementFactory());
		List<ReferenceMapping> entityReferenceMappings = new ArrayList<>();
		List<ReferenceMapping> relReferenceMappings = new ArrayList<>();

		//If there are statements that depend on new nodes i.e. relationships created between new nodes,
		//we must create the new nodes first, and then use their node IDs when creating relationships between them
		if (compiler.hasStatementsDependentOnNewNodes()) {
			boolean manageTransaction = false;
			Transaction tx = session.getTransaction();
			if (tx == null) {
				tx = session.beginTransaction();
				manageTransaction = true;
			}
			DefaultRequest createNodesRowRequest = new DefaultRequest();
			createNodesRowRequest.setStatements(compiler.createNodesStatements());
			try (Response<RowModel> response = session.requestHandler().execute(createNodesRowRequest)) {
				registerNewEntityIds(context, response, entityReferenceMappings);
			}

			List<Statement> statements = new ArrayList<>();
			statements.addAll(compiler.createRelationshipsStatements());
			statements.addAll(compiler.updateNodesStatements());
			statements.addAll(compiler.updateRelationshipStatements());
			statements.addAll(compiler.deleteRelationshipStatements());
			statements.addAll(compiler.deleteRelationshipEntityStatements());

			DefaultRequest defaultRequest = new DefaultRequest();
			defaultRequest.setStatements(statements);
			try (Response<RowModel> response = session.requestHandler().execute(defaultRequest)) {
				registerNewRelIds(response, relReferenceMappings);
			}
			if (manageTransaction) {
				tx.commit();
			}
		} else {
			List<Statement> statements = compiler.getAllStatements();
			if (statements.size() > 0) {
				DefaultRequest defaultRequest = new DefaultRequest();
				defaultRequest.setStatements(statements);
				try (Response<RowModel> response = session.requestHandler().execute(defaultRequest)) {
					registerNewEntityIds(context, response, entityReferenceMappings);
					registerNewRelIds(response, relReferenceMappings);
					// if we had any update statements, then refresh the hashes of the objects
					if(compiler.updateNodesStatements().size()>0) {
						updateSessionContext(context);
					}
				}
			}
		}

		//Update the mapping context now that the request is successful
		updateEntities(context, session, entityReferenceMappings);
		updateEntities(context, session, relReferenceMappings);
		updateRelationships(context, session, relReferenceMappings);
	}

	private void updateSessionContext(CompileContext context) {
		Iterator<Object> iterator = context.registry().iterator();
		while (iterator.hasNext()) {
			Object targetObject = iterator.next();
			if(!(targetObject instanceof TransientRelationship)) {
				ClassInfo classInfo = session.metaData().classInfo(targetObject);
				Field identityField = classInfo.getField(classInfo.identityField());
				Object value = FieldWriter.read(identityField, targetObject);
				if (value != null) session.context().replace(targetObject, (Long) value);
			}
		}
	}

	/**
	 * Execute a save request.
	 * Decides how the request is split depending upon characteristics of what is to be saved.
	 * Processes the response(s) and updates the mapping context.
	 *
	 * @param contexts the CompileContexts for this request
	 */
	public void executeSave(List<CompileContext> contexts) {

		List<ReferenceMapping> entityReferenceMappings = new ArrayList<>();
		List<ReferenceMapping> relReferenceMappings = new ArrayList<>();
		List<Statement> statements = new ArrayList<>();
		boolean manageTransaction = false;
		Transaction tx = session.getTransaction();
		if (tx == null) {
			tx = session.beginTransaction();
			manageTransaction = true;
		}
		for (CompileContext context : contexts) {
			Compiler compiler = context.getCompiler();
			compiler.useStatementFactory(new RowStatementFactory());

			if (compiler.hasStatementsDependentOnNewNodes()) {
				DefaultRequest createNodesRowRequest = new DefaultRequest();
				createNodesRowRequest.setStatements(compiler.createNodesStatements());
				try (Response<RowModel> response = session.requestHandler().execute(createNodesRowRequest)) {
					registerNewEntityIds(context, response, entityReferenceMappings);
					if(compiler.updateNodesStatements().size()>0) {
						updateSessionContext(context);
					}
				}
				statements.addAll(compiler.createRelationshipsStatements());
				statements.addAll(compiler.updateNodesStatements());
				statements.addAll(compiler.updateRelationshipStatements());
				statements.addAll(compiler.deleteRelationshipStatements());
				statements.addAll(compiler.deleteRelationshipEntityStatements());
			}
			else {
				statements.addAll(compiler.getAllStatements());
			}
		}

		//Execute all other statements
		if (statements.size() > 0) {
			DefaultRequest defaultRequest = new DefaultRequest();
			defaultRequest.setStatements(statements);
			try (Response<RowModel> response = session.requestHandler().execute(defaultRequest)) {
				registerNewRelIds(response, relReferenceMappings);
			}
		}
		if (manageTransaction) {
			tx.commit();
		}

		for (CompileContext context : contexts) {
			//Update the mapping context now that the request is successful
			updateEntities(context, session, entityReferenceMappings);
			updateEntities(context, session, relReferenceMappings);
			updateRelationships(context, session, relReferenceMappings);
		}
	}


	/**
	 * Register ids of new entities created in the compile context for use in other parts of the query that depend upon these new entities.
	 * New relationships, for example, may require the IDs of nodes created in the same request.
	 * Note that the mapping context is not updated at this point.
	 *
	 * @param context the compile context
	 * @param response query response
	 * @param entityRefMappings mapping of entity reference used in the compile context and the entity id from the database
	 */
	private void registerNewEntityIds(CompileContext context, Response<RowModel> response, List<ReferenceMapping> entityRefMappings) {
		RowModel rowModel;
		while ((rowModel = response.next()) != null) {
			Object[] results = rowModel.getValues();
			String[] variables = rowModel.variables();
			Long entityRef = null;
			Long entityId = null;
			for (int i = 0; i < variables.length; i++) {
				if (variables[i].equals("nodeId") || variables[i].equals("relId")) {
					entityId = ((Number) results[i]).longValue();
				}
				if (variables[i].equals("nodeRef") || variables[i].endsWith("relRefId")) {
					entityRef = ((Number) results[i]).longValue();
				}
			}
			entityRefMappings.add(new ReferenceMapping(entityRef, entityId));
			context.registerNewNodeId(entityRef, entityId);
		}
	}

	/**
	 * Maintain a mapping of relationship references used in the compile context and the relationship id created in the graph.
	 * Note that the mapping context is not updated at this point
	 *  @param response query response
	 * @param relRefMappings mapping of relationship reference used in the compile context and the relationship id from the database
	 */
	private void registerNewRelIds(Response<RowModel> response, List<ReferenceMapping> relRefMappings) {
		RowModel rowModel;
		while ((rowModel = response.next()) != null) {
			Object[] results = rowModel.getValues();
			Long relRef;
			Long relId;
			relRef = ((Number) results[0]).longValue();
			relId = ((Number) results[1]).longValue();
			relRefMappings.add(new ReferenceMapping(relRef, relId));
		}
	}

	/**
	 * Update the mapping context with entity ids for new nodes and relationship entities created in a request.
	 *
	 * @param context the compile context
	 * @param session the Session
	 * @param entityRefMappings mapping of entity reference used in the compile context and the entity id from the database
	 */
	private void updateEntities(CompileContext context, Neo4jSession session, List<ReferenceMapping> entityRefMappings) {
		for (ReferenceMapping referenceMapping : entityRefMappings) {
			Object persisted = context.getNewObject(referenceMapping.ref);
			registerEntity(referenceMapping.id, persisted, session);
		}
	}

	/**
	 * Update the mapping context with new relationships created in a request.
	 *
	 * @param context the compile context
	 * @param session the Session
	 * @param relRefMappings mapping of relationship reference used in the compile context and the relationship id from the database
	 */
	private void updateRelationships(CompileContext context, Neo4jSession session, List<ReferenceMapping> relRefMappings) {
		for (ReferenceMapping referenceMapping : relRefMappings) {
			for (Object obj : context.registry()) {
				if (obj instanceof TransientRelationship) {
					TransientRelationship transientRelationship = (TransientRelationship) obj;
					if (referenceMapping.ref.equals(transientRelationship.getRef())) {
						MappedRelationship mappedRelationship = new MappedRelationship(context.newNodeId(transientRelationship.getSrc()), transientRelationship.getRel(), context.newNodeId(transientRelationship.getTgt()), transientRelationship.getSrcClass(), transientRelationship.getTgtClass());
						if (session.context().getRelationshipEntity(referenceMapping.id) != null) {
							mappedRelationship.setRelationshipId(referenceMapping.id);
						}
						session.context().mappedRelationships().add(mappedRelationship);
					}
				}
			}
		}
	}

	/**
	 * Register entities in the {@link MappingContext}
	 *
	 * @param persisted entity created as part of the request
	 * @param session the {@link Session}
	 */
	private static void registerEntity(Long identity, Object persisted, Neo4jSession session) {
		MappingContext mappingContext = session.context();
		Transaction tx = session.getTransaction();
		if (persisted != null) {  // it will be null if the variable represents a simple relationship.
			// set the id field of the newly created domain object
			ClassInfo classInfo = session.metaData().classInfo(persisted);
			Field identityField = classInfo.getField(classInfo.identityField());
			FieldWriter.write(identityField, persisted, identity);

			if (tx != null) {
				(( AbstractTransaction ) tx).registerNew( persisted );
			}

			// ensure the newly created domain object is added into the mapping context
			if (classInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
				mappingContext.registerNodeEntity(persisted, identity);
			} else {
				mappingContext.registerRelationshipEntity( persisted, identity );
			}
		}
	}

	class ReferenceMapping {

		private Long ref;
		private Long id;

		ReferenceMapping(Long ref, Long id) {
			this.ref = ref;
			this.id = id;
		}
	}
}

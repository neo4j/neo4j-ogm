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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.compiler.CompileContext;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.context.TransientRelationship;
import org.neo4j.ogm.entity.io.EntityAccessManager;
import org.neo4j.ogm.entity.io.FieldWriter;
import org.neo4j.ogm.entity.io.PropertyReader;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plans request execution and processes the response.
 *
 * @author Luanne Misquitta
 * @author Vince Bickers
 * @author Jasper Blues
 */
public class RequestExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestExecutor.class);

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

		executeSave(context, true);
	}


	/**
	 * Execute a save request.
	 * Decides how the request is split depending upon characteristics of what is to be saved.
	 * Processes the response(s) and updates the mapping context.
	 *
	 * @param contexts the CompileContexts for this request
	 */
	public void executeSave(List<CompileContext> contexts) {

		boolean newTransaction = false;
		Transaction tx = session.getTransaction();

		if (tx == null) {
			tx = session.beginTransaction();
			newTransaction = true;
		}

		for (CompileContext context : contexts) {
			executeSave(context, false);
		}

		if (newTransaction) {
			tx.commit();
			tx.close();
		}
	}

	/**
	 * Register ids of nodes created or updated back into the compile context. New identities are required for use
	 * in other parts of the query that depend upon these new entities. New relationships, for example, may require
	 * the IDs of nodes created in the same request. Existing entity ids are registered because they
	 * will need to be updated in the mapping context
	 *
	 * Note that the mapping context is not updated at this point.
	 *
	 * @param context the compile context
	 * @param response query response
	 * @param entityRefMappings mapping of entity reference used in the compile context and the entity id from the database
	 */
	private void registerEntityIds(CompileContext context, Response<RowModel> response, List<ReferenceMapping> entityRefMappings, List<ReferenceMapping> relEntityRefMappings) {

		RowModel rowModel;

		while ((rowModel = response.next()) != null) {
			Object[] results = rowModel.getValues();
			String[] variables = rowModel.variables();

			Long entityRef = null;

			Long entityId = null;
			String type = null;

			for (int i = 0; i < variables.length; i++) {

				if (variables[i].equals("id")) {
					entityId = ((Number) results[i]).longValue();
				}
				if (variables[i].equals("ref")) {
					entityRef = ((Number) results[i]).longValue();
				}
				if (variables[i].equals("type")) {
					type = (String) results[i];
				}
			}
			if (type != null && type.equals("node")) {
				entityRefMappings.add(new ReferenceMapping(entityRef, entityId));
				if (entityRef!= null && entityRef.equals(entityId)) {
					LOGGER.debug("to update: nodeEntity {}:{}", entityRef, entityId);
				} else {
					LOGGER.debug("to create: nodeEntity {}:{}", entityRef, entityId);
					context.registerNewId(entityRef, entityId);
				}
			}
			else if (type != null && type.equals("rel")) {
				relEntityRefMappings.add(new ReferenceMapping(entityRef, entityId));
				if (entityRef!= null && entityRef.equals(entityId)) {
					LOGGER.debug("to (maybe) update: relEntity {}:{}", entityRef, entityId);
				} else {
					LOGGER.debug("to (maybe) create: relEntity {}:{}", entityRef, entityId);
					context.registerNewId(entityRef, entityId);
				}
			}
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
	 * Update the mapping context with entity ids for new/existing nodes created or updated in the request.
	 *
	 * @param context the compile context
	 * @param session the Session
	 * @param entityRefMappings mapping of entity reference used in the compile context and the entity id from the database
	 */
	private void updateNodeEntities(CompileContext context, Neo4jSession session, List<ReferenceMapping> entityRefMappings) {

		// Ensures the last saved version of existing nodes is current in the cache
		for (Object obj : context.registry()) {
			if (!(obj instanceof TransientRelationship)) {
				ClassInfo classInfo = session.metaData().classInfo(obj);
				if (!classInfo.isRelationshipEntity()) {
					PropertyReader idReader = EntityAccessManager.getIdentityPropertyReader(classInfo);
					Long id = (Long) idReader.readProperty(obj);
					if (id != null) {
						LOGGER.debug("updating existing node id: {}, {}", id, obj);
						registerEntity(session.context(), classInfo, id, obj);
					}
				}
			}
		}

		// Ensures newly created nodes are current in the cache and also assigns their new graph ids
		for (ReferenceMapping referenceMapping : entityRefMappings) {
			// All new objects represented by a reference mapping will have a 'ref' value assigned by us,
			// and a guaranteed-to-be-different 'id' assigned by the db which we collect from the response.
			// These are the objects we need to install and initialise. By contrast, existing objects represented
			// by a reference mapping will always have identical 'ref' and 'id' values.
			if (!(referenceMapping.ref.equals(referenceMapping.id))) {
				Object newEntity = context.getNewObject(referenceMapping.ref);
				LOGGER.debug("creating new node id: {}, {}, {}", referenceMapping.ref, referenceMapping.id, newEntity);
				initialiseNewEntity(referenceMapping.id, newEntity, session);
			}
		}
	}

	/**
	 * Update the mapping context with entity ids for new/existing relationship entities created in a request.
	 *
	 * @param context the compile context
	 * @param session the Session
	 * @param relationshipEntityRefMappings mapping of relationship entity reference used in the compile context and the entity id from the database
	 */
	private void updateRelationshipEntities(CompileContext context, Neo4jSession session, List<ReferenceMapping> relationshipEntityRefMappings) {
		for (ReferenceMapping referenceMapping : relationshipEntityRefMappings) {
			if (referenceMapping.ref.equals(referenceMapping.id)) {
				Object existingRelationshipEntity = session.context().getRelationshipEntity(referenceMapping.id);
				// not all relationship ids represent relationship entities
			    if (existingRelationshipEntity != null) {
					LOGGER.debug("updating existing relationship entity id: {}", referenceMapping.id);
					ClassInfo classInfo = session.metaData().classInfo(existingRelationshipEntity);
					registerEntity(session.context(), classInfo, referenceMapping.id, existingRelationshipEntity);
				}
			} else {
				Object newRelationshipEntity = context.getNewObject(referenceMapping.ref);
				// not all relationship ids represent relationship entities
				if (newRelationshipEntity != null) {
					LOGGER.debug("creating new relationship entity id: {}", referenceMapping.id);
					initialiseNewEntity(referenceMapping.id, newRelationshipEntity, session);
				}
			}
		}
	}

	private void executeSave(CompileContext context, boolean transactionRequired) {

		Compiler compiler = context.getCompiler();
		compiler.useStatementFactory(new RowStatementFactory());

		List<ReferenceMapping> entityReferenceMappings = new ArrayList<>();
		List<ReferenceMapping> relReferenceMappings = new ArrayList<>();

		boolean newTransaction = false;
		Transaction tx = session.getTransaction();
		if (tx == null && transactionRequired) {
			tx = session.beginTransaction();
			newTransaction = true;
		}

		//If there are statements that depend on new nodes i.e. relationships created between new nodes,
		//we must create the new nodes first, and then use their node IDs when creating relationships between them
		if (compiler.hasStatementsDependentOnNewNodes()) {

			DefaultRequest createNodesRowRequest = new DefaultRequest();
			createNodesRowRequest.setStatements(compiler.createNodesStatements());

			// execute the statements to create new nodes. The ids will be returned
			// and will be used in subsequent statements that refer to these new nodes.
			try (Response<RowModel> response = session.requestHandler().execute(createNodesRowRequest)) {
				registerEntityIds(context, response, entityReferenceMappings, relReferenceMappings);
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
				registerEntityIds(context, response, entityReferenceMappings, relReferenceMappings);
				registerNewRelIds(response, relReferenceMappings);
			}

		} else { // only update / delete statements
			List<Statement> statements = compiler.getAllStatements();
			if (statements.size() > 0) {
				DefaultRequest defaultRequest = new DefaultRequest();
				defaultRequest.setStatements(statements);
				try (Response<RowModel> response = session.requestHandler().execute(defaultRequest)) {
					registerEntityIds(context, response, entityReferenceMappings, relReferenceMappings);
					registerNewRelIds(response, relReferenceMappings);
				}
			}
		}

		if (transactionRequired && newTransaction) {
			tx.commit();
			tx.close();
		}

		//Update the mapping context now that the request is successful
		updateNodeEntities(context, session, entityReferenceMappings);
		updateRelationshipEntities(context, session, relReferenceMappings);
		updateRelationships(context, session, relReferenceMappings);
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
						MappedRelationship mappedRelationship = new MappedRelationship(context.getId(transientRelationship.getSrc()), transientRelationship.getRel(), context.getId(transientRelationship.getTgt()), transientRelationship.getSrcClass(), transientRelationship.getTgtClass());
						if (session.context().getRelationshipEntity(referenceMapping.id) != null) {
							mappedRelationship.setRelationshipId(referenceMapping.id);
						}
						session.context().addRelationship(mappedRelationship);
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
	private static void initialiseNewEntity(Long identity, Object persisted, Neo4jSession session) {
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

			registerEntity(mappingContext, classInfo, identity, persisted);
		}
	}

	private static void registerEntity(MappingContext mappingContext, ClassInfo classInfo, Long identity, Object entity) {
		// ensure the newly created domain object is added into the mapping context
		if (classInfo.annotationsInfo().get(RelationshipEntity.CLASS) == null) {
			mappingContext.replaceNodeEntity(entity, identity);      // force the node entity object to be overwritten
		} else {
			mappingContext.replaceRelationshipEntity(entity, identity); // force the relationship entity to be overwritten
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

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

package org.neo4j.ogm.session.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.context.MappedRelationship;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.context.TransientRelationship;
import org.neo4j.ogm.cypher.compiler.CompileContext;
import org.neo4j.ogm.cypher.compiler.Compiler;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.utils.EntityUtils;
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

        Compiler compiler = context.getCompiler();
        compiler.useStatementFactory(new RowStatementFactory());

        List<ReferenceMapping> entityReferenceMappings = new ArrayList<>();
        List<ReferenceMapping> relReferenceMappings = new ArrayList<>();

        boolean forceTx = compiler.updateNodesStatements().stream().anyMatch(st -> st.optimisticLockingConfig().isPresent())
            || compiler.updateRelationshipStatements().stream().anyMatch(st -> st.optimisticLockingConfig().isPresent());

        session.doInTransaction( () -> {

            //If there are statements that depend on new nodes i.e. relationships created between new nodes,
            //we must create the new nodes first, and then use their node IDs when creating relationships between them
            if (compiler.hasStatementsDependentOnNewNodes()) {
                // execute the statements to create new nodes. The ids will be returned
                // and will be used in subsequent statements that refer to these new nodes.
                executeStatements(context, entityReferenceMappings, relReferenceMappings,
                    compiler.createNodesStatements());

                List<Statement> statements = new ArrayList<>();
                statements.addAll(compiler.createRelationshipsStatements());
                statements.addAll(compiler.updateNodesStatements());
                statements.addAll(compiler.updateRelationshipStatements());
                statements.addAll(compiler.deleteRelationshipStatements());
                statements.addAll(compiler.deleteRelationshipEntityStatements());

                executeStatements(context, entityReferenceMappings, relReferenceMappings, statements);
            } else { // only update / delete statements
                List<Statement> statements = compiler.getAllStatements();
                executeStatements(context, entityReferenceMappings, relReferenceMappings, statements);
            }

        }, forceTx, Transaction.Type.READ_WRITE);

        //Update the mapping context now that the request is successful
        updateNodeEntities(context, session, entityReferenceMappings);
        updateRelationshipEntities(context, session, relReferenceMappings);
        updateRelationships(context, session, relReferenceMappings);
    }

    private void executeStatements(CompileContext context, List<ReferenceMapping> entityReferenceMappings,
        List<ReferenceMapping> relReferenceMappings, List<Statement> statements) {
        if (statements.size() > 0) {

            List<Statement> noCheckStatements = new ArrayList<>();
            for (Statement statement : statements) {
                if (statement.optimisticLockingConfig().isPresent()) {
                    DefaultRequest request = new DefaultRequest(statement);
                    try (Response<RowModel> response = session.requestHandler().execute(request)) {
                        List<RowModel> rowModels = response.toList();
                        session.optimisticLockingChecker().checkResultsCount(rowModels, statement);
                        registerEntityIds(context, rowModels, entityReferenceMappings, relReferenceMappings);
                    }
                } else {
                    noCheckStatements.add(statement);
                }
            }

            DefaultRequest defaultRequest = new DefaultRequest();
            defaultRequest.setStatements(noCheckStatements);
            try (Response<RowModel> response = session.requestHandler().execute(defaultRequest)) {
                registerEntityIds(context, response.toList(), entityReferenceMappings, relReferenceMappings);
            }
        }
    }

    /**
     * Register ids of nodes created or updated back into the compile context. New identities are required for use
     * in other parts of the query that depend upon these new entities. New relationships, for example, may require
     * the IDs of nodes created in the same request. Existing entity ids are registered because they
     * will need to be updated in the mapping context
     * Note that the mapping context is not updated at this point.
     *
     * @param context           the compile context
     * @param response          query response
     * @param entityRefMappings mapping of entity reference used in the compile context and the entity id from the database
     */
    private void registerEntityIds(CompileContext context, List<RowModel> response,
        List<ReferenceMapping> entityRefMappings, List<ReferenceMapping> relEntityRefMappings) {

        for (RowModel rowModel : response) {
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
                if (entityRef != null && entityRef.equals(entityId)) {
                    LOGGER.debug("to update: nodeEntity {}:{}", entityRef, entityId);
                } else {
                    LOGGER.debug("to create: nodeEntity {}:{}", entityRef, entityId);
                    context.registerNewId(entityRef, entityId);
                }
            } else if (type != null && type.equals("rel")) {
                relEntityRefMappings.add(new ReferenceMapping(entityRef, entityId));
                if (entityRef != null && entityRef.equals(entityId)) {
                    LOGGER.debug("to (maybe) update: relEntity {}:{}", entityRef, entityId);
                } else {
                    LOGGER.debug("to (maybe) create: relEntity {}:{}", entityRef, entityId);
                    context.registerNewId(entityRef, entityId);
                }
            }
        }
    }

    /**
     * Update the mapping context with entity ids for new/existing nodes created or updated in the request.
     *
     * @param context           the compile context
     * @param session           the Session
     * @param entityRefMappings mapping of entity reference used in the compile context and the entity id from the database
     */
    private void updateNodeEntities(CompileContext context, Neo4jSession session,
        List<ReferenceMapping> entityRefMappings) {

        // Ensures the last saved version of existing nodes is current in the cache
        for (Object obj : context.registry()) {
            if (!(obj instanceof TransientRelationship)) {
                ClassInfo classInfo = session.metaData().classInfo(obj);
                if (!classInfo.isRelationshipEntity()) {
                    Long id = session.context().nativeId(obj);
                    if (id >= 0) {
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
     * @param context                       the compile context
     * @param session                       the Session
     * @param relationshipEntityRefMappings mapping of relationship entity reference used in the compile context and the entity id from the database
     */
    private void updateRelationshipEntities(CompileContext context, Neo4jSession session,
        List<ReferenceMapping> relationshipEntityRefMappings) {
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

    /**
     * Update the mapping context with new relationships created in a request.
     *
     * @param context        the compile context
     * @param session        the Session
     * @param relRefMappings mapping of relationship reference used in the compile context and the relationship id from the database
     */
    private void updateRelationships(CompileContext context, Neo4jSession session,
        List<ReferenceMapping> relRefMappings) {
        final Map<Long, TransientRelationship> registeredTransientRelationshipIndex = buildRegisteredTransientRelationshipIndex(
            context);
        for (ReferenceMapping referenceMapping : relRefMappings) {
            if (registeredTransientRelationshipIndex.containsKey(referenceMapping.ref)) {
                TransientRelationship transientRelationship = registeredTransientRelationshipIndex
                    .get(referenceMapping.ref);
                MappedRelationship mappedRelationship = new MappedRelationship(
                    context.getId(transientRelationship.getSrc()), transientRelationship.getRel(),
                    context.getId(transientRelationship.getTgt()), transientRelationship.getSrcClass(),
                    transientRelationship.getTgtClass());
                if (session.context().getRelationshipEntity(referenceMapping.id) != null) {
                    mappedRelationship.setRelationshipId(referenceMapping.id);
                }
                session.context().addRelationship(mappedRelationship);
            }
        }
    }

    /**
     * Append {@link TransientRelationship} of {@link CompileContext} to an index.
     *
     * @param context the compile context
     * @return an index of {@link TransientRelationship}
     */
    private Map<Long, TransientRelationship> buildRegisteredTransientRelationshipIndex(CompileContext context) {
        final Map<Long, TransientRelationship> transientRelationshipIndex = new HashMap<>();

        for (Object obj : context.registry()) {
            if (TransientRelationship.class.isAssignableFrom(obj.getClass())) {
                TransientRelationship transientRelationship = (TransientRelationship) obj;
                transientRelationshipIndex.put(transientRelationship.getRef(), transientRelationship);
            }
        }

        return transientRelationshipIndex;
    }

    /**
     * Register entities in the {@link MappingContext}
     *
     * @param persisted entity created as part of the request
     * @param session   the {@link Session}
     */
    private void initialiseNewEntity(Long identity, Object persisted, Neo4jSession session) {
        MappingContext mappingContext = session.context();
        Transaction tx = session.getTransaction();
        if (persisted != null) {  // it will be null if the variable represents a simple relationship.
            // set the id field of the newly created domain object
            EntityUtils.setIdentity(persisted, identity, session.metaData());
            ClassInfo classInfo = session.metaData().classInfo(persisted);

            if (tx != null) {
                ((AbstractTransaction) tx).registerNew(persisted);
            }

            registerEntity(mappingContext, classInfo, identity, persisted);
        }
    }

    private void registerEntity(MappingContext mappingContext, ClassInfo classInfo, Long identity, Object entity) {
        // ensure the newly created domain object is added into the mapping context
        if (classInfo.annotationsInfo().get(RelationshipEntity.class) == null) {
            mappingContext.replaceNodeEntity(entity, identity);      // force the node entity object to be overwritten
        } else {
            mappingContext
                .replaceRelationshipEntity(entity, identity); // force the relationship entity to be overwritten
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

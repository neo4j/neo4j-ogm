/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.compiler.v2;


import java.util.*;
import java.util.Map.Entry;

import org.neo4j.ogm.compiler.*;
import org.neo4j.ogm.compiler.Compiler;
import org.neo4j.ogm.compiler.v2.parameters.NewRelationship;
import org.neo4j.ogm.request.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link org.neo4j.ogm.compiler.Compiler} that builds a single query for the object graph.
 *
 * @author Vince Bickers
 */
public class SingleStatementCypherCompiler implements Compiler {

    private final IdentifierManager identifiers = new IdentifierManager();

    private final Set<CypherEmitter> newNodes = new TreeSet<>();
    private final Set<CypherEmitter> updatedNodes = new TreeSet<>();
    private final Set<CypherEmitter> newRelationships = new TreeSet<>();
    private final Set<CypherEmitter> updatedRelationships = new TreeSet<>();
    private final Set<CypherEmitter> deletedRelationships = new TreeSet<>();

    private final CypherEmitter returnClause = new ReturnClauseBuilder();
    private final CompileContext context = new CypherContext();

    private final Logger logger = LoggerFactory.getLogger(SingleStatementCypherCompiler.class);

    @Deprecated
    @Override
    public void relate(String startNode, String relationshipType, Map<String, Object> relationshipProperties, String endNode) {
        RelationshipEmitter newRelationship = newRelationship();
        newRelationship.type(relationshipType);
        for (Entry<String, Object> property : relationshipProperties.entrySet()) {
            newRelationship.addProperty(property.getKey(), property.getValue());
        }
        newRelationship.relate(startNode, endNode);
        newRelationships.add(newRelationship);
    }

    @Override
    public void unrelate(String startNode, String relationshipType, String endNode, Long relId) {
        deletedRelationships.add(new DeletedRelationshipBuilder(relationshipType,startNode, endNode, this.identifiers.nextIdentifier(), relId));
    }

    @Override
    public NodeEmitter newNode() {
        NodeBuilder newNode = new NewNodeBuilder(this.identifiers.nextIdentifier());
        this.newNodes.add(newNode);
        return newNode;
    }

    @Override
    public NodeEmitter existingNode(Long existingNodeId) {
        NodeBuilder node = new ExistingNodeBuilder(this.identifiers.identifier(existingNodeId));
        this.updatedNodes.add(node);
        return node;
    }

    @Override
    public RelationshipEmitter newRelationship() {
        RelationshipBuilder builder = new NewRelationshipBuilder(identifiers.nextIdentifier());
        this.newRelationships.add(builder);
        return builder;
    }

    @Override
    public RelationshipEmitter newBiDirectionalRelationship() {
        RelationshipBuilder builder = new NewBiDirectionalRelationshipBuilder(identifiers.nextIdentifier(), identifiers.nextIdentifier());
        this.newRelationships.add(builder);
        return builder;
    }

    @Override
    public RelationshipEmitter existingRelationship(Long existingRelationshipId) {
        RelationshipBuilder builder = new ExistingRelationshipBuilder(this.identifiers.nextIdentifier(), existingRelationshipId);
        this.updatedRelationships.add(builder);
        return builder;
    }

    @Override
    public List<Statement> getStatements() {

        final StringBuilder queryBuilder = new StringBuilder();
        final Map<String, Object> parameters = new HashMap<>();


        Set<String> varStack = new TreeSet<>();
        Set<String> newStack = new TreeSet<>();

        boolean optimizedCypher = false;
        if (existingRelationshipQuery()) {
            optimizedCypher = buildOptimizedCypher(queryBuilder, varStack, newStack, parameters);
        }

        if(!optimizedCypher) {
            // all create statements can be done in a single clause.
            if (!this.newNodes.isEmpty()) {
                queryBuilder.append(" CREATE ");
                for (Iterator<CypherEmitter> it = this.newNodes.iterator(); it.hasNext(); ) {
                    NodeBuilder node = (NodeBuilder) it.next();
                    if (node.emit(queryBuilder, parameters, varStack)) {
                        newStack.add(node.reference());  // for the return clause
                        if (it.hasNext()) {
                            queryBuilder.append(", ");
                        }
                    }
                }
            }

            for (CypherEmitter emitter : updatedNodes) {
                emitter.emit(queryBuilder, parameters, varStack);
            }
            for (Iterator<CypherEmitter> it = this.newRelationships.iterator(); it.hasNext(); ) {
                RelationshipBuilder relationshipBuilder = (RelationshipBuilder) it.next();
                if (relationshipBuilder.emit(queryBuilder, parameters, varStack)) {
                    newStack.add(relationshipBuilder.reference);
                }
            }

            for (CypherEmitter emitter : updatedRelationships) {
                emitter.emit(queryBuilder, parameters, varStack);
            }

            for (CypherEmitter emitter : deletedRelationships) {
                emitter.emit(queryBuilder, parameters, varStack);
            }

            returnClause.emit(queryBuilder, parameters, newStack);
        }

        Statement statement = new Statement() {
            @Override
            public String getStatement() {
                return queryBuilder.toString().trim();
            }

            @Override
            public Map<String, Object> getParameters() {
                return parameters;
            }
        };

        return Collections.singletonList(statement);
    }

    private boolean buildOptimizedCypher(StringBuilder queryBuilder, Set<String> varStack, Set<String> newStack, Map<String, Object> parameters) {
        boolean optimizedCypher = false;
        Map<String,List<NewRelationship>> newRels = new HashMap<>();
        for (CypherEmitter newRelationship : this.newRelationships) {
			RelationshipBuilder relationshipBuilder = (NewRelationshipBuilder) newRelationship; //TODO group
			if (relationshipBuilder.startNodeIdentifier == null || relationshipBuilder.endNodeIdentifier == null) {
				continue;
			}
			NewRelationship newRel = new NewRelationship(relationshipBuilder.startNodeIdentifier, relationshipBuilder.endNodeIdentifier, relationshipBuilder.reference());
			if (!newRels.containsKey(relationshipBuilder.type)) {
				newRels.put(relationshipBuilder.type, new ArrayList<NewRelationship>());
			}
			newRels.get(relationshipBuilder.type).add(newRel);
		}

        if (newRels.size() > 0) {
			//For each relationship type, build a query and union the results
			for (String relType : newRels.keySet()) {
				for (NewRelationship newRelationship : newRels.get(relType)) {
					varStack.add(newRelationship.getRelRef());
					newStack.add(newRelationship.getRelRef());
				}
				parameters.put("rows" + relType, newRels.get(relType));
				if(queryBuilder.length() > 0) {
					queryBuilder.append(" UNION ALL ");
				}
				OptimizedNewRelationshipBuilder optimizedRelationshipBuilder = new OptimizedNewRelationshipBuilder(relType);
				optimizedRelationshipBuilder.emit(queryBuilder, parameters, varStack);
			}


			optimizedCypher = true;
			logger.debug("Using optimized cypher");
		}
        return optimizedCypher;
    }

    public CompileContext context() {
        return context;
    }

    @Override
    public CompileContext compile() {
        context.setStatements(getStatements());
        return context;
    }

    @Override
    public void release(RelationshipEmitter relationshipBuilder) {
        identifiers.releaseIdentifier();
    }

    @Override
    public String nextIdentifier() {
        return identifiers.nextIdentifier();
    }

    private boolean existingRelationshipQuery() {
        if (this.newNodes.isEmpty()
                && this.updatedRelationships.isEmpty()
                && this.deletedRelationships.isEmpty()
                && !this.newRelationships.isEmpty()) {

            //Check that there are no nodes to be updated
            for (CypherEmitter emitter : updatedNodes) {
                if (!((ExistingNodeBuilder) emitter).props.isEmpty()) {//TODO we should not have such updated nodes in the first place if there are no properties to update
                    return false;
                }
            }

            for (CypherEmitter newRelationship : this.newRelationships) {
                //Check no bidirectional relationships
                if (newRelationship instanceof NewBiDirectionalRelationshipBuilder) {
                    return false;
                }

                //Check no relationship entities
                RelationshipBuilder relationshipBuilder = (NewRelationshipBuilder) newRelationship;
                if (relationshipBuilder.isRelationshipEntity()) {
                    return false;
                }
            }
            return true;

        }
        return false;
    }

}

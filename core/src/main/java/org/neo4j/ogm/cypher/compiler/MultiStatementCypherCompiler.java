/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.cypher.compiler;

import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.compiler.builders.node.DefaultNodeBuilder;
import org.neo4j.ogm.cypher.compiler.builders.node.DefaultRelationshipBuilder;
import org.neo4j.ogm.cypher.compiler.builders.statement.DeletedRelationshipEntityStatementBuilder;
import org.neo4j.ogm.cypher.compiler.builders.statement.DeletedRelationshipStatementBuilder;
import org.neo4j.ogm.cypher.compiler.builders.statement.ExistingNodeStatementBuilder;
import org.neo4j.ogm.cypher.compiler.builders.statement.ExistingRelationshipStatementBuilder;
import org.neo4j.ogm.cypher.compiler.builders.statement.NewNodeStatementBuilder;
import org.neo4j.ogm.cypher.compiler.builders.statement.NewRelationshipStatementBuilder;
import org.neo4j.ogm.exception.core.UnknownStatementTypeException;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;
import org.neo4j.ogm.response.model.RelationshipModel;

/**
 * Cypher compiler that produces multiple statements that can be executed together or split over a transaction.
 *
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class MultiStatementCypherCompiler implements Compiler {

    private final CompileContext context;
    private final List<NodeBuilder> newNodeBuilders;
    private final List<RelationshipBuilder> newRelationshipBuilders;
    private final Map<Long, NodeBuilder> existingNodeBuilders;
    private final Map<String, RelationshipBuilder> existingRelationshipBuilders;
    private final List<RelationshipBuilder> deletedRelationshipBuilders;
    private final List<RelationshipBuilder> deletedRelationshipEntityBuilders;
    private StatementFactory statementFactory;

    public MultiStatementCypherCompiler(Function<Object, Long> nativeIdProvider) {
        this.context = new CypherContext(this, nativeIdProvider);
        this.newNodeBuilders = new ArrayList<>();
        this.newRelationshipBuilders = new ArrayList<>();
        this.existingNodeBuilders = new HashMap<>();
        this.existingRelationshipBuilders = new LinkedHashMap<>(); // Order for the relationships IS important
        this.deletedRelationshipBuilders = new ArrayList<>();
        this.deletedRelationshipEntityBuilders = new ArrayList<>();
    }

    @Override
    public NodeBuilder newNode(Long id) {
        NodeBuilder nodeBuilder = new DefaultNodeBuilder(id);
        newNodeBuilders.add(nodeBuilder);
        return nodeBuilder;
    }

    @Override
    public RelationshipBuilder newRelationship(String type, boolean bidirectional) {
        RelationshipBuilder relationshipBuilder = new DefaultRelationshipBuilder(type, bidirectional);
        newRelationshipBuilders.add(relationshipBuilder);
        return relationshipBuilder;
    }

    @Override
    public RelationshipBuilder newRelationship(String type) {
        return newRelationship(type, false);
    }

    @Override
    public NodeBuilder existingNode(Long existingNodeId) {
        return existingNodeBuilders.computeIfAbsent(existingNodeId, DefaultNodeBuilder::new);
    }

    @Override
    public RelationshipBuilder existingRelationship(Long existingRelationshipId, Relationship.Direction direction, String type, boolean wasDirty) {
        String key = existingRelationshipId + ";" + direction.name();
        RelationshipBuilder relationshipBuilder = existingRelationshipBuilders.computeIfAbsent(key, k -> new DefaultRelationshipBuilder(type, existingRelationshipId));
        relationshipBuilder.setDirty(wasDirty);
        return relationshipBuilder;
    }

    @Override
    public RelationshipBuilder unrelate(Long startNode, String relationshipType, Long endNode, Long relId) {
        RelationshipBuilder relationshipBuilder = new DefaultRelationshipBuilder(relationshipType, relId);
        relationshipBuilder.relate(startNode, endNode);
        if (!unmap(relationshipBuilder)) {
            if (relId != null) {
                deletedRelationshipEntityBuilders.add(relationshipBuilder);
            } else {
                deletedRelationshipBuilders.add(relationshipBuilder);
            }
        }
        return relationshipBuilder;
    }

    @Override
    public void unmap(NodeBuilder nodeBuilder) {

        if (nodeBuilder.reference() != null) {
            existingNodeBuilders.remove(nodeBuilder.reference());
        } else {
            Iterator<Map.Entry<Long, NodeBuilder>> it = existingNodeBuilders.entrySet().iterator();
            while (it.hasNext()) {
                NodeBuilder builder = it.next().getValue();
                if (builder == nodeBuilder) {
                    it.remove();
                }
            }
        }
    }

    public List<Statement> createNodesStatements() {
        assertStatementFactoryExists();
        Map<String, Set<Node>> newNodesByLabels = groupNodesByLabel(newNodeBuilders);
        List<Statement> statements = new ArrayList<>(newNodesByLabels.size());
        for (Set<Node> nodeModels : newNodesByLabels.values()) {
            NewNodeStatementBuilder newNodeBuilder = new NewNodeStatementBuilder(nodeModels, statementFactory);
            statements.add(newNodeBuilder.build());
        }

        return statements;
    }

    @Override
    public List<Statement> createRelationshipsStatements() {
        assertStatementFactoryExists();
        //Group relationships by type and non-null properties
        //key: relationship type, value: Map where key=Set<Property strings>, value: Set of edges with those properties
        Map<String, Map<String, Set<Edge>>> relsByTypeAndProps = new HashMap<>();
        for (RelationshipBuilder relationshipBuilder : newRelationshipBuilders) {
            if (relationshipBuilder.edge().getStartNode() == null || relationshipBuilder.edge().getEndNode() == null) {
                continue; //TODO this is a carry forward from the old emitters. We want to prevent this rel builder getting created or remove it
            }
            Map<String, Set<Edge>> relsByProps = relsByTypeAndProps
                .computeIfAbsent(relationshipBuilder.type(), (key) -> new HashMap<>());

            RelationshipModel edge = (RelationshipModel) relationshipBuilder.edge();

            String primaryId = edge.getPrimaryIdName();

            Set<Edge> rels = relsByProps.computeIfAbsent(primaryId, (s) -> new HashSet<>());
            edge.setStartNode(context.getId(edge.getStartNode()));
            edge.setEndNode(context.getId(edge.getEndNode()));
            rels.add(edge);
        }

        List<Statement> statements = new ArrayList<>();
        //For each relationship type
        for (Map<String, Set<Edge>> edgesByProperties : relsByTypeAndProps.values()) {
            //For each set of unique property keys
            for (Set<Edge> edges : edgesByProperties.values()) {
                NewRelationshipStatementBuilder newRelationshipBuilder = new NewRelationshipStatementBuilder(edges,
                    statementFactory);
                statements.add(newRelationshipBuilder.build());
            }
        }

        return statements;
    }

    @Override
    public List<Statement> updateNodesStatements() {
        assertStatementFactoryExists();
        Map<String, Set<Node>> existingNodesByLabels = groupNodesByLabel(existingNodeBuilders.values());
        List<Statement> statements = new ArrayList<>(existingNodesByLabels.size());
        for (Set<Node> nodeModels : existingNodesByLabels.values()) {
            ExistingNodeStatementBuilder existingNodeBuilder = new ExistingNodeStatementBuilder(nodeModels,
                statementFactory);
            statements.add(existingNodeBuilder.build());
        }

        return statements;
    }

    @Override
    public List<Statement> updateRelationshipStatements() {
        assertStatementFactoryExists();

        if (existingRelationshipBuilders.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Boolean, Set<Edge>> collect = existingRelationshipBuilders.values().stream()
            .collect(partitioningBy(RelationshipBuilder::isDirty, Collectors.mapping(RelationshipBuilder::edge, Collectors.toSet())));

        List<Statement> result = new ArrayList<>();
        if (!collect.get(true).isEmpty()) {
            ExistingRelationshipStatementBuilder builder = new ExistingRelationshipStatementBuilder(collect.get(true), statementFactory, true);
            result.add(builder.build());
        }

        if (!collect.get(false).isEmpty()) {
            ExistingRelationshipStatementBuilder builder = new ExistingRelationshipStatementBuilder(collect.get(false), statementFactory, false);
            result.add(builder.build());
        }

        return result;
    }

    @Override
    public List<Statement> deleteRelationshipStatements() {
        assertStatementFactoryExists();
        //Group relationships by type
        Map<String, Set<Edge>> deletedRelsByType = groupRelationshipsByType(deletedRelationshipBuilders);
        List<Statement> statements = new ArrayList<>();

        for (Set<Edge> edges : deletedRelsByType.values()) {
            DeletedRelationshipStatementBuilder deletedRelationshipBuilder = new DeletedRelationshipStatementBuilder(
                edges, statementFactory);
            statements.add(deletedRelationshipBuilder.build());
        }
        return statements;
    }

    @Override
    public List<Statement> deleteRelationshipEntityStatements() {
        assertStatementFactoryExists();
        //Group relationships by type
        Map<String, Set<Edge>> deletedRelsByType = groupRelationshipsByType(deletedRelationshipEntityBuilders);

        List<Statement> statements = new ArrayList<>();

        for (Set<Edge> edges : deletedRelsByType.values()) {
            DeletedRelationshipEntityStatementBuilder deletedRelationshipBuilder = new DeletedRelationshipEntityStatementBuilder(
                edges, statementFactory);
            statements.add(deletedRelationshipBuilder.build());
        }
        return statements;
    }

    @Override
    public List<Statement> getAllStatements() {

        List<Statement> statements = new ArrayList<>();
        statements.addAll(createNodesStatements());
        statements.addAll(createRelationshipsStatements());
        statements.addAll(updateNodesStatements());
        statements.addAll(updateRelationshipStatements());
        statements.addAll(deleteRelationshipStatements());
        statements.addAll(deleteRelationshipEntityStatements());
        return statements;
    }

    @Override
    public CompileContext context() {
        return context;
    }

    @Override
    public boolean hasStatementsDependentOnNewNodes() {
        for (RelationshipBuilder builder : newRelationshipBuilders) {
            Edge edge = builder.edge();
            //TODO the null check is a carry forward from the old cypher builders. We want to prevent this rel builder getting created or remove it
            if ((edge.getStartNode() != null && edge.getStartNode() < 0)
                || (edge.getEndNode() != null && edge.getEndNode() < 0)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void useStatementFactory(StatementFactory newStatementFactory) {
        this.statementFactory = newStatementFactory;
    }

    private boolean unmap(RelationshipBuilder relationshipBuilder) {
        boolean unmapped = false;
        Iterator<RelationshipBuilder> relIterator = newRelationshipBuilders.iterator();
        while (relIterator.hasNext()) {
            RelationshipBuilder newRelBuilder = relIterator.next();
            if (relationshipBuilder.reference() >= 0) {
                if (relationshipBuilder.reference().equals(newRelBuilder.reference())) {
                    relIterator.remove();
                    unmapped = true;
                    break;
                }
            } else {
                if (relationshipBuilder.type().equals(newRelBuilder.type())
                    && relationshipBuilder.edge().getStartNode().equals(newRelBuilder.edge().getStartNode())
                    && relationshipBuilder.edge().getEndNode().equals(newRelBuilder.edge().getEndNode())) {
                    relIterator.remove();
                    unmapped = true;
                    break;
                }
            }
        }
        return unmapped;
    }

    private void assertStatementFactoryExists() {
        if (statementFactory == null) {
            throw new UnknownStatementTypeException("Unknown statement type- statementFactory must be specified!");
        }
    }

    private Map<String, Set<Node>> groupNodesByLabel(Collection<NodeBuilder> nodeBuilders) {
        return nodeBuilders.stream()
            .map(NodeBuilder::node)
            .collect(groupingBy(Node::labelSignature, Collectors.mapping(Function.identity(), Collectors.toSet())));
    }

    private Map<String, Set<Edge>> groupRelationshipsByType(List<RelationshipBuilder> relationshipBuilders) {
        Map<String, Set<Edge>> relsByType = new HashMap<>();
        for (RelationshipBuilder relationshipBuilder : relationshipBuilders) {
            if (!relsByType.containsKey(relationshipBuilder.type())) {
                relsByType.put(relationshipBuilder.type(), new HashSet<>());
            }
            //Replace the node ids
            RelationshipModel edge = (RelationshipModel) relationshipBuilder.edge();
            edge.setStartNode(context.getId(edge.getStartNode()));
            edge.setEndNode(context.getId(edge.getEndNode()));
            relsByType.get(relationshipBuilder.type()).add(edge);
            relsByType.get(relationshipBuilder.type()).add(edge);
        }
        return relsByType;
    }

}

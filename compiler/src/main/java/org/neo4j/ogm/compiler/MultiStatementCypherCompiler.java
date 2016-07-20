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

package org.neo4j.ogm.compiler;

import org.neo4j.ogm.compiler.builders.DefaultNodeBuilder;
import org.neo4j.ogm.compiler.builders.DefaultRelationshipBuilder;
import org.neo4j.ogm.compiler.emitters.*;
import org.neo4j.ogm.exception.UnknownStatementTypeException;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;
import org.neo4j.ogm.response.model.RelationshipModel;

import java.util.*;

/**
 * Cypher compiler that produces multiple statements that can be executed together or split over a transaction.
 *
 * @author Luanne Misquitta
 */
public class MultiStatementCypherCompiler implements Compiler {

	private final CompileContext context = new CypherContext(this);
	private List<NodeBuilder> newNodeBuilders = new ArrayList<>();
	private List<RelationshipBuilder> newRelationshipBuilders = new ArrayList<>();
	private List<NodeBuilder> existingNodeBuilders = new ArrayList<>();
	private List<RelationshipBuilder> existingRelationshipBuilders = new ArrayList<>();
	private List<RelationshipBuilder> deletedRelationshipBuilders = new ArrayList<>();
	private List<RelationshipBuilder> deletedRelationshipEntityBuilders = new ArrayList<>();
	private StatementFactory statementFactory;

	@Override
	public org.neo4j.ogm.compiler.NodeBuilder newNode(Long id) {
		org.neo4j.ogm.compiler.NodeBuilder nodeBuilder = new DefaultNodeBuilder(id);
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
		org.neo4j.ogm.compiler.NodeBuilder nodeBuilder = new DefaultNodeBuilder(existingNodeId);
		existingNodeBuilders.add(nodeBuilder);
		return nodeBuilder;
	}

	@Override
	public RelationshipBuilder existingRelationship(Long existingRelationshipId, String type) {
		RelationshipBuilder relationshipBuilder = new DefaultRelationshipBuilder(type, existingRelationshipId);
		existingRelationshipBuilders.add(relationshipBuilder);
		return relationshipBuilder;
	}

	@Override
	public void unrelate(Long startNode, String relationshipType, Long endNode, Long relId) {
		RelationshipBuilder relationshipBuilder = new DefaultRelationshipBuilder(relationshipType, relId);
		relationshipBuilder.relate(startNode, endNode);
		if (!unmap(relationshipBuilder)) {
			if (relId != null) {
				deletedRelationshipEntityBuilders.add(relationshipBuilder);
			} else {
				deletedRelationshipBuilders.add(relationshipBuilder);
			}
		}
	}

	@Override
	public void unmap(NodeBuilder nodeBuilder) {
		existingNodeBuilders.remove(nodeBuilder);
	}


	public List<Statement> createNodesStatements() {
		assertStatementFactoryExists();
		Map<String, Set<Node>> newNodesByLabels = groupNodesByLabel(newNodeBuilders);
		List<Statement> statements = new ArrayList<>(newNodesByLabels.size());
		for (Set<Node> nodeModels : newNodesByLabels.values()) {
			NewNodeEmitter newNodeEmitter = new NewNodeEmitter(nodeModels);
			final Map<String, Object> parameters = new HashMap<>();
			final StringBuilder query = new StringBuilder();
			newNodeEmitter.emit(query, parameters);
			statements.add(statementFactory.statement(query.toString(), parameters));
		}

		return statements;
	}



	@Override
	public List<Statement> createRelationshipsStatements() {
		assertStatementFactoryExists();
		//Group relationships by type and non-null properties
		//key: relationship type, value: Map where key=Set<Property strings>, value: Set of edges with those properties
		Map<String, Map<Set<String>, Set<Edge>>> relsByTypeAndProps = new HashMap<>();
		for (RelationshipBuilder relationshipBuilder : newRelationshipBuilders) {
			if (relationshipBuilder.edge().getStartNode() == null || relationshipBuilder.edge().getEndNode() == null) {
				continue; //TODO this is a carry forward from the old emitters. We want to prevent this rel builder getting created or remove it
			}
			if (!relsByTypeAndProps.containsKey(relationshipBuilder.type())) {
				relsByTypeAndProps.put(relationshipBuilder.type(), new HashMap<Set<String>, Set<Edge>>());
			}
			RelationshipModel edge = (RelationshipModel) relationshipBuilder.edge();
			Set<String> nonNullPropertyKeys = new HashSet<>();
			Iterator<Property<String, Object>> propertyIterator = edge.getPropertyList().iterator();
			while (propertyIterator.hasNext()) {
				Property property = propertyIterator.next();
				if (property.getValue() == null) {
					propertyIterator.remove();
				} else {
					nonNullPropertyKeys.add((String) property.getKey());
				}
			}
			if (!relsByTypeAndProps.get(relationshipBuilder.type()).containsKey(nonNullPropertyKeys)) {
				relsByTypeAndProps.get(relationshipBuilder.type()).put(nonNullPropertyKeys, new HashSet<Edge>());
			}
			edge.setStartNode(context.getId(edge.getStartNode()));
			edge.setEndNode(context.getId(edge.getEndNode()));
			relsByTypeAndProps.get(relationshipBuilder.type()).get(nonNullPropertyKeys).add(edge);
		}

		List<Statement> statements = new ArrayList<>();
		//For each relationship type
		for (Map<Set<String>, Set<Edge>> edgesByProperties : relsByTypeAndProps.values()) {
			//For each set of unique property keys
			for (Set<Edge> edges : edgesByProperties.values()) {
				NewRelationshipEmitter newRelationshipEmitter = new NewRelationshipEmitter(edges);
				final Map<String, Object> parameters = new HashMap<>();
				final StringBuilder query = new StringBuilder();
				newRelationshipEmitter.emit(query, parameters);
				statements.add(statementFactory.statement(query.toString(), parameters));
			}
		}

		return statements;
	}

	@Override
	public List<Statement> updateNodesStatements() {
		assertStatementFactoryExists();
		Map<String, Set<Node>> existingNodesByLabels = groupNodesByLabel(existingNodeBuilders);

		List<Statement> statements = new ArrayList<>(existingNodesByLabels.size());
		for (Set<Node> nodeModels : existingNodesByLabels.values()) {
			ExistingNodeEmitter existingNodeEmitter = new ExistingNodeEmitter(nodeModels);
			final Map<String, Object> parameters = new HashMap<>();
			final StringBuilder query = new StringBuilder();
			existingNodeEmitter.emit(query, parameters);
			statements.add(statementFactory.statement(query.toString(), parameters));
		}

		return statements;
	}

	@Override
	public List<Statement> updateRelationshipStatements() {
		assertStatementFactoryExists();
		Set<Edge> relationships = new HashSet<>(existingRelationshipBuilders.size());
		List<Statement> statements = new ArrayList<>(existingRelationshipBuilders.size());
		if (existingRelationshipBuilders.size() > 0) {
			for (RelationshipBuilder relBuilder : existingRelationshipBuilders) {
				relationships.add(relBuilder.edge());
			}
			final Map<String, Object> parameters = new HashMap<>();
			final StringBuilder query = new StringBuilder();
			ExistingRelationshipEmitter existingRelationshipEmitter = new ExistingRelationshipEmitter(relationships);
			existingRelationshipEmitter.emit(query, parameters);
			statements.add(statementFactory.statement(query.toString(), parameters));
		}
		return statements;
	}

	@Override
	public List<Statement> deleteRelationshipStatements() {
		assertStatementFactoryExists();
		//Group relationships by type
		Map<String, Set<Edge>> deletedRelsByType = groupRelationshipsByType(deletedRelationshipBuilders);
		List<Statement> statements = new ArrayList<>();

		for (Set<Edge> edges : deletedRelsByType.values()) {
			DeletedRelationshipEmitter deletedRelationshipEmitter = new DeletedRelationshipEmitter(edges);
			final Map<String, Object> parameters = new HashMap<>();
			final StringBuilder query = new StringBuilder();
			deletedRelationshipEmitter.emit(query, parameters);
			statements.add(statementFactory.statement(query.toString(), parameters));
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
			DeletedRelationshipEntityEmitter deletedRelationshipEmitter = new DeletedRelationshipEntityEmitter(edges);
			final Map<String, Object> parameters = new HashMap<>();
			final StringBuilder query = new StringBuilder();
			deletedRelationshipEmitter.emit(query, parameters);
			statements.add(statementFactory.statement(query.toString(), parameters));
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
			//TODO the null check is a carry forward from the old emitters. We want to prevent this rel builder getting created or remove it
			if ((edge.getStartNode()!=null && edge.getStartNode() < 0)
					|| (edge.getEndNode()!=null && edge.getEndNode() < 0)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void useStatementFactory(StatementFactory statementFactory) {
		this.statementFactory = statementFactory;
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

	private Map<String, Set<Node>> groupNodesByLabel(List<NodeBuilder> nodeBuilders) {
		Map<String, Set<Node>> nodesByLabels = new HashMap<>();
		for (NodeBuilder nodeBuilder : nodeBuilders) {
			//String joinedLabels = String.join(",", nodeBuilder.addedLabels());
			String joinedLabels = join(nodeBuilder.addedLabels());
			if (!nodesByLabels.containsKey(joinedLabels)) {
				nodesByLabels.put(joinedLabels, new HashSet<Node>());
			}
			nodesByLabels.get(joinedLabels).add(nodeBuilder.node());
		}
		return nodesByLabels;
	}

	private Map<String, Set<Edge>> groupRelationshipsByType(List<RelationshipBuilder> relationshipBuilders) {
		Map<String, Set<Edge>> relsByType = new HashMap<>();
		for (RelationshipBuilder relationshipBuilder : relationshipBuilders) {
			if (!relsByType.containsKey(relationshipBuilder.type())) {
				relsByType.put(relationshipBuilder.type(), new HashSet<Edge>());
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

	private String join(String[] parts) { //TODO drop when we start compiling with Java 8
		StringBuilder str = new StringBuilder();
		for (String part : parts) {
			if (str.length() > 0) {
				str.append(",");
			}
			str.append(part);
		}
		return str.toString();
	}
}

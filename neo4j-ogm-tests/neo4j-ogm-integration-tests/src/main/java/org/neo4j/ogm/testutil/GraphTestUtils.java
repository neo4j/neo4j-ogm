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
package org.neo4j.ogm.testutil;

import static org.neo4j.graphdb.Direction.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import org.junit.Assert;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestDatabaseManagementServiceBuilder;

/**
 * Utility methods used to facilitate testing against a real Neo4j database.
 *
 * @author Michal Bachman
 * @author Mark Angrish
 */
@SuppressWarnings("HiddenField")
public final class GraphTestUtils {

    private static GraphDatabaseService otherDatabase = new TestDatabaseManagementServiceBuilder()
        .impermanent()
        .build()
        .database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);

    private GraphTestUtils() {
        // this class cannot be instantiated
    }

    /**
     * Checks that the graph in the specified {@link GraphDatabaseService} is the same as the graph that the given cypher
     * produces.
     *
     * @param graphDatabase   The {@link GraphDatabaseService} to check
     * @param sameGraphCypher The Cypher create statement, which communicates the desired state of the database
     * @throws AssertionError if the cypher doesn't produce a graph that matches the state of the given database
     */
    public static synchronized void assertSameGraph(GraphDatabaseService graphDatabase, String sameGraphCypher) {

        otherDatabase.executeTransactionally(sameGraphCypher);

        try {
            try (Transaction tx = graphDatabase.beginTx()) {
                try (Transaction tx2 = otherDatabase.beginTx()) {
                    doAssertSubgraph(graphDatabase, otherDatabase, "existing database");
                    doAssertSubgraph(otherDatabase, graphDatabase, "Cypher-created database");
                    tx2.rollback();
                }
                tx.rollback();
            }
        } finally {
            otherDatabase.executeTransactionally("MATCH (n) OPTIONAL MATCH (n) DETACH DELETE n");
        }
    }

    private static void doAssertSubgraph(GraphDatabaseService database, GraphDatabaseService otherDatabase,
        String firstDatabaseName) {
        Map<Long, Long[]> sameNodesMap = buildSameNodesMap(database, otherDatabase, firstDatabaseName);
        Set<Map<Long, Long>> nodeMappings = buildNodeMappingPermutations(sameNodesMap, otherDatabase);

        if (nodeMappings.size() == 1) {
            assertRelationshipsMappingExistsForSingleNodeMapping(database, otherDatabase,
                nodeMappings.iterator().next(), firstDatabaseName);
            return;
        }

        for (Map<Long, Long> nodeMapping : nodeMappings) {
            if (relationshipsMappingExists(database, otherDatabase, nodeMapping)) {
                return;
            }
        }

        Assert.fail("There is no corresponding relationship mapping for any of the possible node mappings");
    }

    private static Set<Map<Long, Long>> buildNodeMappingPermutations(Map<Long, Long[]> sameNodesMap,
        GraphDatabaseService otherDatabase) {
        Set<Map<Long, Long>> result = new HashSet<>();
        result.add(new HashMap<>());

        try (Transaction tx = otherDatabase.beginTx()) {
            for (Map.Entry<Long, Long[]> entry : sameNodesMap.entrySet()) {

                Set<Map<Long, Long>> newResult = new HashSet<>();

                for (Long target : entry.getValue()) {
                    for (Map<Long, Long> mapping : result) {
                        if (!mapping.values().contains(target)) {
                            Map<Long, Long> newMapping = new HashMap<>(mapping);
                            newMapping.put(entry.getKey(), target);
                            newResult.add(newMapping);
                        }
                    }
                }

                if (newResult.isEmpty()) {
                    Assert
                        .fail("Could not find a node corresponding to: " + print(tx.getNodeById(entry.getKey()))
                            + ". There are most likely more nodes with the same characteristics (labels, properties) in your "
                            + "cypher CREATE statement but fewer in the database.");
                }

                result = newResult;
            }
        }

        return result;
    }

    public static Iterable<Node> allNodes(GraphDatabaseService graphDatabaseService) {

        try {
            Method allNodes = GraphDatabaseService.class.getMethod("getAllNodes");
            return (Iterable<Node>) allNodes.invoke(graphDatabaseService);
        } catch (NoSuchMethodException nsme) {
            try {
                Class clazz = Class.forName("org.neo4j.tooling.GlobalGraphOperations");
                try {
                    Method at = clazz.getMethod("at", GraphDatabaseService.class);
                    Object instance = at.invoke(null, graphDatabaseService);
                    Method allNodes = instance.getClass().getMethod("getAllNodes");
                    return (Iterable<Node>) allNodes.invoke(instance);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException nsme2) {
                    throw new RuntimeException(nsme2);
                }
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            }
        } catch (InvocationTargetException | IllegalAccessException ite) {
            throw new RuntimeException(ite);
        }
    }

    public static Iterable<Relationship> allRelationships(GraphDatabaseService graphDatabaseService) {

        try {
            Method allRelationships = GraphDatabaseService.class.getMethod("getAllRelationships");
            return (Iterable<Relationship>) allRelationships.invoke(graphDatabaseService);
        } catch (NoSuchMethodException nsme) {
            try {
                Class clazz = Class.forName("org.neo4j.tooling.GlobalGraphOperations");
                try {
                    Method at = clazz.getMethod("at", GraphDatabaseService.class);
                    Object instance = at.invoke(null, graphDatabaseService);
                    Method allRelationships = instance.getClass().getMethod("getAllRelationships");
                    return (Iterable<Relationship>) allRelationships.invoke(instance);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException nsme2) {
                    throw new RuntimeException(nsme2);
                }
            } catch (ClassNotFoundException cnfe) {
                throw new RuntimeException(cnfe);
            }
        } catch (InvocationTargetException | IllegalAccessException ite) {
            throw new RuntimeException(ite);
        }
    }

    private static Map<Long, Long[]> buildSameNodesMap(GraphDatabaseService database,
        GraphDatabaseService otherDatabase,
        String firstDatabaseName) {
        Map<Long, Long[]> sameNodesMap = new HashMap<>(); //map of nodeID and IDs of nodes that match

        for (Node node : allNodes(otherDatabase)) {

            Iterable<Node> sameNodes = findSameNodes(database, node); //List of all nodes that match this

            //fail fast
            if (!sameNodes.iterator().hasNext()) {
                Assert.fail("There is no corresponding node to " + print(node) + " in " + firstDatabaseName);
            }

            Set<Long> sameNodeIds = new HashSet<>();
            for (Node sameNode : sameNodes) {
                sameNodeIds.add(sameNode.getId());
            }
            sameNodesMap.put(node.getId(), sameNodeIds.toArray(new Long[sameNodeIds.size()]));
        }

        return sameNodesMap;
    }

    public static Iterable<Node> findSameNodes(GraphDatabaseService database, Node node) {
        Iterator<Label> labels = node.getLabels().iterator();
        if (labels.hasNext()) {
            return findSameNodesByLabel(database, node, labels.next());
        }

        return findSameNodesWithoutLabel(database, node);
    }

    public static Iterable<Node> findSameNodesWithoutLabel(GraphDatabaseService database, Node node) {
        Set<Node> result = new HashSet<>();

        for (Node candidate : allNodes(database)) {
            if (areSame(node, candidate)) {
                result.add(candidate);
            }
        }

        return result;
    }

    public static Iterable<Node> nodesWithLabel(GraphDatabaseService database, Label label) {
        Set<Node> result = new HashSet<>();

        for (Node node : allNodes(database)) {
            if (node.hasLabel(label)) {
                result.add(node);
            }
        }
        return result;
    }

    public static Iterable<Node> findSameNodesByLabel(GraphDatabaseService database, Node node, Label label) {
        Set<Node> result = new HashSet<>();

        for (Node candidate : nodesWithLabel(database, label)) {
            if (areSame(node, candidate)) {
                result.add(candidate);
            }
        }

        return result;
    }

    private static void assertRelationshipsMappingExistsForSingleNodeMapping(GraphDatabaseService database,
        GraphDatabaseService otherDatabase, Map<Long, Long> mapping, String firstDatabaseName) {
        Set<Long> usedRelationships = new HashSet<>();

        try (Transaction tx = database.beginTx()) {
            for (Relationship relationship : allRelationships(otherDatabase)) {
                if (!relationshipMappingExists(tx, relationship, mapping, usedRelationships)) {
                    Assert.fail(
                        "No corresponding relationship found to " + print(relationship) + " in " + firstDatabaseName);
                }
            }
        }
    }

    private static boolean relationshipsMappingExists(GraphDatabaseService database, GraphDatabaseService otherDatabase,
        Map<Long, Long> mapping) {
        Set<Long> usedRelationships = new HashSet<>();
        try (Transaction tx = database.beginTx()) {
            for (Relationship relationship : allRelationships(otherDatabase)) {
                if (!relationshipMappingExists(tx, relationship, mapping, usedRelationships)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean relationshipMappingExists(Transaction database, Relationship relationship,
        Map<Long, Long> nodeMapping,
        Set<Long> usedRelationships) {
        for (Relationship candidate : database.getNodeById(nodeMapping.get(relationship.getStartNode().getId()))
            .getRelationships(OUTGOING)) {
            if (nodeMapping.get(relationship.getEndNode().getId()).equals(candidate.getEndNode().getId())) {
                if (areSame(candidate, relationship) && !usedRelationships.contains(candidate.getId())) {
                    usedRelationships.add(candidate.getId());
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean areSame(Node node1, Node node2) {
        return haveSameLabels(node1, node2) && haveSameProperties(node1, node2);
    }

    public static boolean areSame(Relationship relationship1, Relationship relationship2) {
        return haveSameType(relationship1, relationship2) && haveSameProperties(relationship1, relationship2);
    }

    private static long count(Iterable<?> iterable) {

        return StreamSupport.stream(iterable.spliterator(), false).count();
    }

    public static boolean haveSameLabels(Node node1, Node node2) {
        if (count(node1.getLabels()) != count(node2.getLabels())) {
            return false;
        }

        for (Label label : node1.getLabels()) {
            if (!node2.hasLabel(label)) {
                return false;
            }
        }

        return true;
    }

    public static boolean haveSameType(Relationship relationship1, Relationship relationship2) {
        return relationship1.isType(relationship2.getType());
    }

    public static boolean haveSameProperties(Entity pc1, Entity pc2) {
        int pc1KeyCount = 0;
        int pc2KeyCount = 0;
        for (String key : pc1.getPropertyKeys()) {
            pc1KeyCount++;
            if (!pc2.hasProperty(key)) {
                return false;
            }
            if (!stringRepresentationsMatch(pc1.getProperty(key), pc2.getProperty(key))) {
                return false;
            }
        }
        for (Iterator<String> it = pc2.getPropertyKeys().iterator(); it.hasNext(); it.next()) {
            pc2KeyCount++;
        }
        return pc1KeyCount == pc2KeyCount;
    }

    private static String print(Node node) {
        StringBuilder string = new StringBuilder("(");

        Collection<String> labelNames = new TreeSet<>();
        for (Label label : node.getLabels()) {
            labelNames.add(label.name());
        }

        for (String labelName : labelNames) {
            string.append(":").append(labelName);
        }

        String props = propertiesToString(node);
        if (!(props.isEmpty() || labelNames.isEmpty())) {
            string.append(" ");
        }
        string.append(props);
        string.append(")");
        return string.toString();
    }

    private static String print(Relationship relationship) {
        StringBuilder string = new StringBuilder();

        string.append(print(relationship.getStartNode()));
        string.append("-[:").append(relationship.getType().name());
        String props = propertiesToString(relationship);
        if (!props.isEmpty()) {
            string.append(" ");
        }
        string.append(props);
        string.append("]->");
        string.append(print(relationship.getEndNode()));

        return string.toString();
    }

    private static String propertiesToString(Entity propertyContainer) {
        if (!propertyContainer.getPropertyKeys().iterator().hasNext()) {
            return "";
        }

        StringBuilder string = new StringBuilder("{");

        Collection<String> propertyKeys = new TreeSet<>();
        for (String key : propertyContainer.getPropertyKeys()) {
            propertyKeys.add(key);
        }

        for (String key : propertyKeys) {
            string.append(key).append(": ").append(propertyValueToString(propertyContainer.getProperty(key)))
                .append(", ");
        }
        string.setLength(string.length() - 2);

        string.append("}");

        return string.toString();
    }

    private static boolean stringRepresentationsMatch(Object one, Object other) {
        String oneString = propertyValueToString(one);
        String otherString = propertyValueToString(other);
        return oneString.equals(otherString);
    }

    private static String propertyValueToString(Object o) {
        if (o instanceof byte[]) {
            return Arrays.toString((byte[]) o);
        }
        if (o instanceof char[]) {
            return Arrays.toString((char[]) o);
        }
        if (o instanceof boolean[]) {
            return Arrays.toString((boolean[]) o);
        }
        if (o instanceof long[]) {
            return Arrays.toString((long[]) o);
        }
        if (o instanceof double[]) {
            return Arrays.toString((double[]) o);
        }
        if (o instanceof int[]) {
            return Arrays.toString((int[]) o);
        }
        if (o instanceof short[]) {
            return Arrays.toString((short[]) o);
        }
        if (o instanceof float[]) {
            return Arrays.toString((float[]) o);
        }
        if (o instanceof String[]) {
            return Arrays.toString((String[]) o);
        }
        return String.valueOf(o);
    }
}

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
package org.neo4j.ogm.autoindex;

import static java.util.Collections.*;
import static org.neo4j.ogm.autoindex.IndexType.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.session.request.RowDataStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an Index that can be auto generated in Neo4j.
 *
 * @author Mark Angrish
 * @author Eric Spiegelberg
 */
class AutoIndex {

    private static final Logger logger = LoggerFactory.getLogger(AutoIndex.class);

    private static final Set<String> ENTITIES_IN_LOOKUP_INDIZES = Collections
        .unmodifiableSet(new HashSet<>(Arrays.asList("node", "relationship")));

    private final String[] properties;

    private final String name;

    /**
     * Owning type - either node label or relationship type
     */
    private final String owningType;

    /**
     * Type of the index/constraint
     */
    private final IndexType type;

    /**
     * This is a cypher fragment that is used during create and drop for constraints.
     */
    private final String description;

    AutoIndex(Class<?> targetEntity, IndexType type, String owningType, String[] properties, String optionalName) {

        if (optionalName != null) {
            this.name = optionalName;
        } else if (targetEntity != null) {
            this.name = targetEntity.getCanonicalName().toLowerCase(Locale.ROOT).replace(".", "_") + "_" + String.join("_", properties) + (type.isConstraint() ? "_" + type.name().toLowerCase(Locale.ROOT).replace("_constraint", "") : "");
        } else {
            this.name = null;
        }
        this.properties = properties;
        this.owningType = owningType;
        this.type = type;
        this.description = createDescription(name, type, owningType, properties);
    }

    private static String createDescription(String name, IndexType type, String owningType, String[] properties) {

        String variable = owningType.toLowerCase();

        if (name == null) {
            switch (type) {
                case SINGLE_INDEX:
                    validatePropertiesLength(properties, SINGLE_INDEX);
                    return "INDEX ON :`" + owningType + "`(`" + properties[0] + "`)";

                case UNIQUE_CONSTRAINT:
                    validatePropertiesLength(properties, UNIQUE_CONSTRAINT);
                    return "CONSTRAINT ON (`" + variable + "`:`" + owningType + "`) ASSERT `" + variable + "`.`"
                        + properties[0]
                        + "` IS UNIQUE";

                case COMPOSITE_INDEX:
                    return buildCompositeIndex(name, variable, owningType, properties);

                case NODE_KEY_CONSTRAINT:
                    return buildNodeKeyConstraint(name, variable, owningType, properties);

                case NODE_PROP_EXISTENCE_CONSTRAINT:
                    validatePropertiesLength(properties, NODE_PROP_EXISTENCE_CONSTRAINT);
                    return "CONSTRAINT ON (`" + variable + "`:`" + owningType + "`) ASSERT exists(`" + variable + "`.`"
                        + properties[0] + "`)";

                case REL_PROP_EXISTENCE_CONSTRAINT:
                    validatePropertiesLength(properties, NODE_PROP_EXISTENCE_CONSTRAINT);
                    return "CONSTRAINT ON ()-[`" + variable + "`:`" + owningType + "`]-() ASSERT exists(`" + variable
                        + "`.`"
                        + properties[0] + "`)";

                default:
                    throw new UnsupportedOperationException("Index type " + type + " not supported yet");
            }
        } else {
            switch (type) {
                case SINGLE_INDEX:
                    validatePropertiesLength(properties, SINGLE_INDEX);
                    return "INDEX " + name + " FOR (`" + variable + "`:`" + owningType + "`) ON (`" + variable + "`.`"
                        + properties[0] + "`)";

                case UNIQUE_CONSTRAINT:
                    validatePropertiesLength(properties, UNIQUE_CONSTRAINT);
                    return "CONSTRAINT " + name + " FOR (`" + variable + "`:`" + owningType + "`) REQUIRE `" + variable
                        + "`.`"
                        + properties[0]
                        + "` IS UNIQUE";

                case COMPOSITE_INDEX:
                    return buildCompositeIndex(name, variable, owningType, properties);

                case NODE_KEY_CONSTRAINT:
                    return buildNodeKeyConstraint(name, variable, owningType, properties);

                case NODE_PROP_EXISTENCE_CONSTRAINT:
                    validatePropertiesLength(properties, NODE_PROP_EXISTENCE_CONSTRAINT);
                    return "CONSTRAINT " + name + " FOR (`" + variable + "`:`" + owningType + "`) REQUIRE `" + variable
                        + "`.`"
                        + properties[0] + "` IS NOT NULL";

                case REL_PROP_EXISTENCE_CONSTRAINT:
                    validatePropertiesLength(properties, NODE_PROP_EXISTENCE_CONSTRAINT);
                    return "CONSTRAINT " + name + " FOR ()-[`" + variable + "`:`" + owningType + "`]-() REQUIRE `"
                        + variable
                        + "`.`"
                        + properties[0] + "` IS NOT NULL";

                default:
                    throw new UnsupportedOperationException("Index type " + type + " not supported yet");
            }
        }
    }

    private static void validatePropertiesLength(String[] properties, IndexType violatedIndexType) {

        if (properties.length != 1) {
            throw new IllegalArgumentException(
                violatedIndexType + " must have exactly one property, got " +
                    Arrays.toString(properties));
        }
    }

    private static String buildCompositeIndex(String name, String variable, String owningType, String[] properties) {

        StringBuilder sb = new StringBuilder();
        if (name == null) {
            sb.append("INDEX ON :`")
                .append(owningType)
                .append("`(");
            appendProperties(sb, properties);
        } else {
            sb.append("INDEX ").append(name).append(" FOR (`" + variable + "`:`")
                .append(owningType)
                .append("`) ON (");
            appendPropertiesWithNode(sb, variable, properties);
        }
        sb.append(")");
        return sb.toString();
    }

    private static String buildNodeKeyConstraint(String name, String variable, String owningType, String[] properties) {

        StringBuilder sb = new StringBuilder();
        if (name == null) {
            sb.append("CONSTRAINT ON (`")
                .append(variable)
                .append("`:`")
                .append(owningType)
                .append("`) ASSERT (");
        } else {
            sb.append("CONSTRAINT ").append(name).append(" FOR (`")
                .append(variable)
                .append("`:`")
                .append(owningType)
                .append("`) REQUIRE (");
        }
        appendPropertiesWithNode(sb, variable, properties);
        sb.append(") IS NODE KEY");
        return sb.toString();
    }

    private static void appendProperties(StringBuilder sb, String[] properties) {
        for (int i = 0; i < properties.length; i++) {
            sb.append('`');
            sb.append(properties[i]);
            sb.append('`');
            if (i < (properties.length - 1)) {
                sb.append(',');
            }
        }
    }

    private static void appendPropertiesWithNode(StringBuilder sb, String nodeName, String[] properties) {
        for (int i = 0; i < properties.length; i++) {
            sb.append('`');
            sb.append(nodeName);
            sb.append("`.`");
            sb.append(properties[i]);
            sb.append('`');
            if (i < (properties.length - 1)) {
                sb.append(',');
            }
        }
    }

    public String[] getProperties() {
        return properties;
    }

    public String getOwningType() {
        return owningType;
    }

    public IndexType getType() {
        return type;
    }

    Statement getCreateStatement() {
        return new RowDataStatement("CREATE " + this.description, emptyMap());
    }

    public Statement getDropStatement() {
        var statement = "DROP " + (this.name != null && this.description.contains(" FOR ") ?
            this.description.substring(0, this.description.indexOf(this.name) + this.name.length()) :
            this.description);
        return new RowDataStatement(statement, emptyMap());
    }

    String getDescription() {
        return description;
    }

    static Optional<AutoIndex> parseConstraint(Map<String, Object> constraintRow) {

        String name = (String) constraintRow.get("name");
        String description = (String) constraintRow.get("description");
        String type = (String) constraintRow.get("type");
        String entityType = (String) constraintRow.get("entityType");
        String[] labelsOrTypes = (String[]) constraintRow.get("labelsOrTypes");
        String[] properties = (String[]) constraintRow.get("properties");

        if ("UNIQUENESS".equals(type) && "NODE".equals(entityType)) {
            return Optional.of(new AutoIndex(null, IndexType.UNIQUE_CONSTRAINT, labelsOrTypes[0], properties, name));
        } else if ("NODE_KEY".equals(type)) {
            return Optional.of(new AutoIndex(null, IndexType.NODE_KEY_CONSTRAINT, labelsOrTypes[0], properties, name));
        } else if ("NODE_PROPERTY_EXISTENCE".equals(type)) {
            return Optional.of(
                new AutoIndex(null, IndexType.NODE_PROP_EXISTENCE_CONSTRAINT, labelsOrTypes[0], properties, name));
        } else if ("RELATIONSHIP_PROPERTY_EXISTENCE".equals(type)) {
            return Optional.of(
                new AutoIndex(null, IndexType.REL_PROP_EXISTENCE_CONSTRAINT, labelsOrTypes[0], properties, name));
        }

        logger.warn("Could not parse constraint description {}", description);
        return Optional.empty();
    }

    static boolean isFulltext(Map<String, Object> indexRow) {

        String indexType = (String) indexRow.get("type");

        return indexType != null && indexType.toLowerCase(Locale.ENGLISH).contains("fulltext");
    }

    static boolean isNodeOrRelationshipLookup(Map<String, Object> indexRow) {

        String indexType = (String) indexRow.get("type");
        String entityType = (String) indexRow.get("entityType");

        return indexType != null && entityType != null &&
            indexType.toLowerCase(Locale.ENGLISH).trim().equals("lookup") && ENTITIES_IN_LOOKUP_INDIZES
            .contains(entityType.trim().toLowerCase(Locale.ENGLISH));
    }

    static Optional<AutoIndex> parseIndex(Map<String, Object> indexRow) {

        String name = indexRow.containsKey("name") ? (String) indexRow.get("name") : null;
        String description = (String) indexRow.get("description");
        String indexType = (String) indexRow.get("type");

        if (isFulltext(indexRow)) {
            logger.warn("Ignoring unsupported fulltext index.");
            return Optional.empty();
        }

        if (isNodeOrRelationshipLookup(indexRow)) {
            logger.info("The Node and Relationship lookups available in Neo4j 4.3+ should not be modified and Neo4j-OGM wont touch it.");
            return Optional.empty();
        }

        // skip unique properties index because they will get processed within
        // the collection of constraints.
        if (indexRow.containsKey("uniqueness")) {
            String indexUniqueness = (String) indexRow.get("uniqueness");
            if (indexUniqueness.equals("UNIQUE")) {
                return Optional.empty();
            }
        }

        if (indexRow.containsKey("properties") && indexRow.containsKey("labelsOrTypes") && indexRow.get("labelsOrTypes") instanceof String[]) {
            String[] indexProperties = (String[]) indexRow.get("properties");
            String indexLabel = ((String[]) indexRow.get("labelsOrTypes"))[0];

            return Optional.of(new AutoIndex(null, indexProperties.length > 1 ? COMPOSITE_INDEX : SINGLE_INDEX,
                indexLabel, indexProperties, name));
        }

        logger.warn("Could not parse index of type {} with description {}", indexType, description);
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AutoIndex autoIndex = (AutoIndex) o;
        return Arrays.equals(properties, autoIndex.properties) &&
            owningType.equals(autoIndex.owningType) &&
            type == autoIndex.type;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(owningType, type);
        result = 31 * result + Arrays.hashCode(properties);
        return result;
    }

    @Override
    public String toString() {
        return "AutoIndex{" +
            "description='" + description + '\'' +
            '}';
    }

    public boolean hasOpposite() {
        switch (type) {
            case SINGLE_INDEX:
            case COMPOSITE_INDEX:
            case UNIQUE_CONSTRAINT:
            case NODE_KEY_CONSTRAINT:
                return true;

            default:
                return false;
        }
    }

    public AutoIndex createOppositeIndex() {
        switch (type) {
            case SINGLE_INDEX:
                return new AutoIndex(null, UNIQUE_CONSTRAINT, owningType, properties, name);

            case UNIQUE_CONSTRAINT:
                return new AutoIndex(null, SINGLE_INDEX, owningType, properties, name);

            case COMPOSITE_INDEX:
                return new AutoIndex(null, NODE_KEY_CONSTRAINT, owningType, properties, name);

            case NODE_KEY_CONSTRAINT:
                return new AutoIndex(null, COMPOSITE_INDEX, owningType, properties, name);

            default:
                throw new IllegalStateException("Can not create opposite index for type=" + type);
        }
    }
}

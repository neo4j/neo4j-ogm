/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
import static java.util.Optional.*;
import static java.util.regex.Pattern.*;
import static org.neo4j.ogm.autoindex.IndexType.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final String[] properties;

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

    AutoIndex(IndexType type, String owningType, String[] properties) {
        this.properties = properties;
        this.owningType = owningType;
        this.type = type;
        this.description = createDescription(type, owningType, properties);
    }

    private static String createDescription(IndexType type, String owningType, String[] properties) {

        String name = owningType.toLowerCase();

        switch (type) {
            case SINGLE_INDEX:
                validatePropertiesLength(properties, SINGLE_INDEX);
                return "INDEX ON :`" + owningType + "`(`" + properties[0] + "`)";

            case UNIQUE_CONSTRAINT:
                validatePropertiesLength(properties, UNIQUE_CONSTRAINT);
                return "CONSTRAINT ON (`" + name + "`:`" + owningType + "`) ASSERT `" + name + "`.`" + properties[0]
                    + "` IS UNIQUE";

            case COMPOSITE_INDEX:
                return buildCompositeIndex(name, owningType, properties);

            case NODE_KEY_CONSTRAINT:
                return buildNodeKeyConstraint(name, owningType, properties);

            case NODE_PROP_EXISTENCE_CONSTRAINT:
                validatePropertiesLength(properties, NODE_PROP_EXISTENCE_CONSTRAINT);
                return "CONSTRAINT ON (`" + name + "`:`" + owningType + "`) ASSERT exists(`" + name + "`.`"
                    + properties[0] + "`)";

            case REL_PROP_EXISTENCE_CONSTRAINT:
                validatePropertiesLength(properties, NODE_PROP_EXISTENCE_CONSTRAINT);
                return "CONSTRAINT ON ()-[`" + name + "`:`" + owningType + "`]-() ASSERT exists(`" + name + "`.`"
                    + properties[0] + "`)";

            default:
                throw new UnsupportedOperationException("Index type " + type + " not supported yet");
        }
    }

    private static void validatePropertiesLength(String[] properties, IndexType violatedIndexType) {

        if (properties.length != 1) {
            throw new IllegalArgumentException(
                violatedIndexType + " must have exactly one property, got " +
                    Arrays.toString(properties));
        }
    }

    private static String buildCompositeIndex(String name, String owningType, String[] properties) {

        StringBuilder sb = new StringBuilder();
        sb.append("INDEX ON :`")
            .append(owningType)
            .append("`(");
        appendProperties(sb, properties);
        sb.append(")");
        return sb.toString();
    }

    private static String buildNodeKeyConstraint(String name, String owningType, String[] properties) {

        StringBuilder sb = new StringBuilder();
        sb.append("CONSTRAINT ON (`")
            .append(name)
            .append("`:`")
            .append(owningType)
            .append("`) ASSERT (");
        appendPropertiesWithNode(sb, name, properties);
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
        return new RowDataStatement("DROP " + this.description, emptyMap());
    }

    String getDescription() {
        return description;
    }

    static Optional<AutoIndex> parseConstraint(Map<String, Object> constraintRow, String version) {

        Pattern pattern;
        Matcher matcher;

        String description = (String) constraintRow.get("description");
        if (version.compareTo("4.0") < 0) {

            pattern = compile(
                "CONSTRAINT ON \\((?<name>.*):(?<label>.*)\\) ASSERT ?\\k<name>.(?<property>.*) IS UNIQUE");
            matcher = pattern.matcher(description);
            if (matcher.matches()) {
                String label = matcher.group("label").trim();
                String[] properties = matcher.group("property").split(",");
                return of(new AutoIndex(IndexType.UNIQUE_CONSTRAINT, label, properties));
            }

            pattern = compile(
                "CONSTRAINT ON \\((?<name>.*):(?<label>.*)\\) ASSERT \\((?<properties>.*)\\) IS NODE KEY");
            matcher = pattern.matcher(description);
            if (matcher.matches()) {
                String label = matcher.group("label").trim();
                String[] properties = matcher.group("properties").split(",");
                for (int i = 0; i < properties.length; i++) {
                    properties[i] = properties[i].trim().substring(label.length() + 1);
                }
                return of(new AutoIndex(IndexType.NODE_KEY_CONSTRAINT, label, properties));
            }

            pattern = compile(
                "CONSTRAINT ON \\(\\s?(?<name>.*):(?<label>.*)\\s?\\) ASSERT exists\\(?\\k<name>.(?<property>.*)\\)");
            matcher = pattern.matcher(description);
            if (matcher.matches()) {
                String label = matcher.group("label").trim();
                String[] properties = matcher.group("property").split(",");
                return of(new AutoIndex(IndexType.NODE_PROP_EXISTENCE_CONSTRAINT, label, properties));
            }

            pattern = compile(
                "CONSTRAINT ON \\(\\)-\\[\\s?(?<name>.*):(?<label>.*)\\s?\\]-\\(\\) ASSERT exists\\(?\\k<name>.(?<property>.*)\\)");
            matcher = pattern.matcher(description);
            if (matcher.matches()) {
                String label = matcher.group("label").trim();
                String[] properties = matcher.group("property").split(",");
                for (int i = 0; i < properties.length; i++) {
                    properties[i] = properties[i].trim();
                }
                return of(new AutoIndex(IndexType.REL_PROP_EXISTENCE_CONSTRAINT, label, properties));
            }

            logger.warn("Could not parse constraint description {}", description);
        }
        pattern = compile(
            "CONSTRAINT ON \\( ?(?<name>.+):(?<label>.+) ?\\) ASSERT \\(\\k<name>\\.(?<property>.*)\\) IS UNIQUE");
        matcher = pattern.matcher(description);
        if (matcher.matches()) {
            String label = matcher.group("label").trim();
            String[] properties = matcher.group("property").split(",");
            return of(new AutoIndex(IndexType.UNIQUE_CONSTRAINT, label, properties));
        }

        pattern = compile(
            "CONSTRAINT ON \\((?<name>.*):(?<label>.*)\\) ASSERT \\((?<properties>.*)\\) IS NODE KEY");
        matcher = pattern.matcher(description);
        if (matcher.matches()) {
            String label = matcher.group("label").trim();
            String[] properties = matcher.group("properties").split(",");
            for (int i = 0; i < properties.length; i++) {
                properties[i] = properties[i].trim().substring(label.length() + 1);
            }
            return of(new AutoIndex(IndexType.NODE_KEY_CONSTRAINT, label, properties));
        }

        pattern = compile(
            "CONSTRAINT ON \\(\\s?(?<name>.*):(?<label>.*)\\s?\\) ASSERT exists\\(?\\k<name>.(?<property>.*)\\)");
        matcher = pattern.matcher(description);
        if (matcher.matches()) {
            String label = matcher.group("label").trim();
            String[] properties = matcher.group("property").split(",");
            return of(new AutoIndex(IndexType.NODE_PROP_EXISTENCE_CONSTRAINT, label, properties));
        }

        pattern = compile(
            "CONSTRAINT ON \\(\\)-\\[\\s?(?<name>.*):(?<label>.*)\\s?\\]-\\(\\) ASSERT exists\\(?\\k<name>.(?<property>.*)\\)");
        matcher = pattern.matcher(description);
        if (matcher.matches()) {
            String label = matcher.group("label").trim();
            String[] properties = matcher.group("property").split(",");
            for (int i = 0; i < properties.length; i++) {
                properties[i] = properties[i].trim();
            }
            return of(new AutoIndex(IndexType.REL_PROP_EXISTENCE_CONSTRAINT, label, properties));
        }

        logger.warn("Could not parse constraint description {}", description);
        return empty();
    }

    static Optional<AutoIndex> parseIndex(Map<String, Object> indexRow, String version) {

        Pattern pattern;
        Matcher matcher;

        String description = (String) indexRow.get("description");
        String indexType = (String) indexRow.get("type");

        if (indexType != null && indexType.toLowerCase(Locale.ENGLISH).contains("fulltext")) {
            logger.warn("Ignoring unsupported index type {}.", indexType);
            return Optional.empty();
        }

        if (version.compareTo("4.0") < 0) {
            // skip unique properties index because they will get processed within
            // the collection of constraints.
            if (indexType.equals("node_unique_property")) {
                return empty();
            }

            pattern = compile("INDEX ON :(?<label>.*)\\((?<property>.*)\\)");
            matcher = pattern.matcher(description);
            if (matcher.matches()) {
                String label = matcher.group("label");
                String[] properties = matcher.group("property").split(",");
                for (int i = 0; i < properties.length; i++) {
                    properties[i] = properties[i].trim();
                }
                if (properties.length > 1) {
                    return of(new AutoIndex(IndexType.COMPOSITE_INDEX, label, properties));
                } else {
                    return of(new AutoIndex(SINGLE_INDEX, label, properties));
                }
            }
        }

        // skip unique properties index because they will get processed within
        // the collection of constraints.
        if (indexRow.containsKey("uniqueness")) {
            String indexUniqueness = (String) indexRow.get("uniqueness");
            if (indexUniqueness.equals("UNIQUE")) {
                return empty();
            }
        }

        if (indexRow.containsKey("properties") && indexRow.containsKey("labelsOrTypes")) {
            String[] indexProperties = (String[]) indexRow.get("properties");
            String indexLabel = ((String[]) indexRow.get("labelsOrTypes"))[0];

            return of(new AutoIndex(indexProperties.length > 1 ? COMPOSITE_INDEX : SINGLE_INDEX,
                indexLabel, indexProperties));
        }

        logger.warn("Could not parse index of type {} with description {}", indexType, description);
        return empty();
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
                return new AutoIndex(UNIQUE_CONSTRAINT, owningType, properties);

            case UNIQUE_CONSTRAINT:
                return new AutoIndex(SINGLE_INDEX, owningType, properties);

            case COMPOSITE_INDEX:
                return new AutoIndex(NODE_KEY_CONSTRAINT, owningType, properties);

            case NODE_KEY_CONSTRAINT:
                return new AutoIndex(COMPOSITE_INDEX, owningType, properties);

            default:
                throw new IllegalStateException("Can not create opposite index for type=" + type);
        }
    }
}

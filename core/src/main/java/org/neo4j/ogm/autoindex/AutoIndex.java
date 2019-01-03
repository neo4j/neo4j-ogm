/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

    private String createDescription(IndexType type, String owningType, String[] properties) {

        String name = owningType.toLowerCase();

        switch (type) {
            case SINGLE_INDEX:
                if (properties.length != 1) {
                    throw new IllegalArgumentException(SINGLE_INDEX + " must have exactly one property, got " +
                        Arrays.toString(properties));
                }
                return "INDEX ON :`" + owningType + "`(`" + properties[0] + "`)";

            case UNIQUE_CONSTRAINT:
                if (properties.length != 1) {
                    throw new IllegalArgumentException(UNIQUE_CONSTRAINT + " must have exactly one property, got " +
                        Arrays.toString(properties));
                }
                return "CONSTRAINT ON (`" + name + "`:`" + owningType + "`) ASSERT `" + name + "`.`" + properties[0]
                    + "` IS UNIQUE";

            case COMPOSITE_INDEX: {
                StringBuilder sb = new StringBuilder();
                sb.append("INDEX ON :`")
                    .append(owningType)
                    .append("`(");
                appendProperties(sb, properties);
                sb.append(")");
                return sb.toString();
            }

            case NODE_KEY_CONSTRAINT: {
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

            case NODE_PROP_EXISTENCE_CONSTRAINT:
                if (properties.length != 1) {
                    throw new IllegalArgumentException(
                        NODE_PROP_EXISTENCE_CONSTRAINT + " must have exactly one property, got " +
                            Arrays.toString(properties));
                }
                return "CONSTRAINT ON (`" + name + "`:`" + owningType + "`) ASSERT exists(`" + name + "`.`"
                    + properties[0] + "`)";

            case REL_PROP_EXISTENCE_CONSTRAINT:
                if (properties.length != 1) {
                    throw new IllegalArgumentException(
                        NODE_PROP_EXISTENCE_CONSTRAINT + " must have exactly one property, got " +
                            Arrays.toString(properties));
                }
                return "CONSTRAINT ON ()-[`" + name + "`:`" + owningType + "`]-() ASSERT exists(`" + name + "`.`"
                    + properties[0] + "`)";

            default:
                throw new UnsupportedOperationException("Index type " + type + " not supported yet");
        }
    }

    private void appendProperties(StringBuilder sb, String[] properties) {
        for (int i = 0; i < properties.length; i++) {
            sb.append('`');
            sb.append(properties[i]);
            sb.append('`');
            if (i < (properties.length - 1)) {
                sb.append(',');
            }
        }
    }

    private void appendPropertiesWithNode(StringBuilder sb, String nodeName, String[] properties) {
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

    public static Optional<AutoIndex> parse(String description) {

        Pattern pattern;
        Matcher matcher;

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

        pattern = compile("CONSTRAINT ON \\((?<name>.*):(?<label>.*)\\) ASSERT ?\\k<name>.(?<property>.*) IS UNIQUE");
        matcher = pattern.matcher(description);
        if (matcher.matches()) {
            String label = matcher.group("label").trim();
            String[] properties = matcher.group("property").split(",");
            return of(new AutoIndex(IndexType.UNIQUE_CONSTRAINT, label, properties));
        }

        pattern = compile("CONSTRAINT ON \\((?<name>.*):(?<label>.*)\\) ASSERT \\((?<properties>.*)\\) IS NODE KEY");
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

        logger.warn("Could not parse index description {}", description);

        return empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AutoIndex autoIndex = (AutoIndex) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(properties, autoIndex.properties))
            return false;
        if (!owningType.equals(autoIndex.owningType))
            return false;
        return type == autoIndex.type;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(properties);
        result = 31 * result + owningType.hashCode();
        result = 31 * result + type.hashCode();
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

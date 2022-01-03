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
package org.neo4j.ogm.drivers.bolt.response;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.summary.InputPosition;
import org.neo4j.driver.summary.Notification;
import org.neo4j.driver.summary.ResultSummary;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public abstract class BoltResponse<T> implements Response {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoltResponse.class);

    protected final Result result;

    BoltResponse(Result result) {
        this.result = result;
    }

    @Override
    public T next() {
        try {
            return fetchNext();
        } catch (ClientException ce) {
            LOGGER.debug("Error executing Cypher: {}, {}", ce.code(), ce.getMessage());
            throw new CypherException(ce.code(), ce.getMessage(), ce);
        }
    }

    protected abstract T fetchNext();

    @Override
    public void close() {
        // Consume the rest of the result and thus closing underlying resources.
        ResultSummary summary = result.consume();
        process(summary);
    }

    @Override
    public String[] columns() {
        if (result.hasNext()) {
            Record record = result.peek();
            if (record != null) {
                Set<String> columns = result.peek().asMap().keySet();
                return columns.toArray(new String[columns.size()]);
            }
        }
        return new String[0];
    }

    private static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Does some post processing on the giving result summary, especially logging all notifications
     * and potentially query plans.
     *
     * @param resultSummary The result summary to process
     * @return The same, unmodified result summary.
     */
    static ResultSummary process(ResultSummary resultSummary) {
        logNotifications(resultSummary);
        return resultSummary;
    }

    private static void logNotifications(ResultSummary resultSummary) {

        if (resultSummary.notifications().isEmpty() || !LOGGER.isWarnEnabled()) {
            return;
        }

        String query = resultSummary.query().text();
        resultSummary.notifications()
            .forEach(notification -> {
                Consumer<String> log;
                switch (notification.severity()) {
                    case "WARNING":
                        log = LOGGER::warn;
                        break;
                    case "INFORMATION":
                        log = LOGGER::info;
                        break;
                    default:
                        log = LOGGER::debug;
                }
                log.accept(format(notification, query));
            });
    }

    /**
     * Creates a formatted string for a notification issued for a given query.
     *
     * @param notification The notification to format
     * @param forQuery     The query that caused the notification
     * @return A formatted string
     */
    static String format(Notification notification, String forQuery) {

        InputPosition position = notification.position();

        StringBuilder queryHint = new StringBuilder();
        String[] lines = forQuery.split("(\r\n|\n)");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            queryHint.append("\t").append(line).append(LINE_SEPARATOR);
            if (i + 1 == position.line()) {
                queryHint.append("\t").append(Stream.generate(() -> " ").limit(position.column() - 1)
                    .collect(Collectors.joining())).append("^").append(System.lineSeparator());
            }
        }
        return String.format("%s: %s%n%s%s", notification.code(), notification.title(), queryHint,
            notification.description());
    }
}

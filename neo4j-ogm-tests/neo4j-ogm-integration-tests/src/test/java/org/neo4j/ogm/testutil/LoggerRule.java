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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.LoggerFactory;

/**
 * Naive rule to capture logging output. It assumes that logback is used during tests as SLF4J binding.
 *
 * @author Michael J. Simons
 */
public class LoggerRule implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

	private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
	private final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	public List<String> getFormattedMessages() {
		return listAppender.list.stream().map(e -> e.getFormattedMessage()).collect(Collectors.toList());
	}

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        logger.addAppender(listAppender);
        listAppender.start();
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        listAppender.stop();
        listAppender.list.clear();
        logger.detachAppender(listAppender);
    }
}

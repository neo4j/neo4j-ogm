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

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public final class TestUtils {

    public static int getAvailablePort() {
        try {
            try (ServerSocket socket = new ServerSocket(0)) {
                return socket.getLocalPort();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find available port: " + e.getMessage(), e);
        }
    }

    public static <T> T firstOrNull(Iterable<T> iterable) {
        return firstOrNull(iterable.iterator());
    }

    public static <T> T firstOrNull(Iterator<T> iterator) {
        return iterator.hasNext() ? iterator.next() : null;
    }

    public static StringBuilder readCQLFile(String cqlFileName) {

        StringBuilder cypher = new StringBuilder();
        try (Scanner scanner = new Scanner(
            Thread.currentThread().getContextClassLoader().getResourceAsStream(cqlFileName))) {
            scanner.useDelimiter(System.getProperty("line.separator"));
            while (scanner.hasNext()) {
                cypher.append(scanner.next()).append(' ');
            }
        }
        return cypher;
    }

    private TestUtils() {
    }
}

/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.testutil;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author Vince Bickers
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
        try (Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader().getResourceAsStream(cqlFileName))) {
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

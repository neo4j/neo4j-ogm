/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.integration;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.ogm.session.Session;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;

public class IntegrationTest     {

    private static NeoServer neoServer;
    protected static int neoPort;

    protected static Session session;

    @BeforeClass
    public static void setUp() throws IOException {
        neoPort = getAvailablePort();
        neoServer = CommunityServerBuilder.server().onPort(neoPort).build();
        neoServer.start();
    }

    @AfterClass
    public static void tearDown() {
        neoServer.stop();
    }

    private static int getAvailablePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            try {
                return socket.getLocalPort();
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find available port: " + e.getMessage(), e);
        }
    }

    protected static String load(String cqlFile) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(cqlFile)));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}

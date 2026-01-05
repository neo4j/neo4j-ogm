/*
 * Copyright (c) 2002-2026 "Neo4j,"
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
package org.neo4j.ogm.config;

public interface DatabaseSelectionProvider {

	DatabaseSelection getDatabaseSelection();

	/**
	 * A user selection provider always selecting the connected user.
	 *
	 * @return A provider for using the connected user.
	 */
	static DatabaseSelectionProvider getDefaultSelectionProvider() {

		return DefaultDatabaseSelectionProvider.INSTANCE;
	}
}

enum DefaultDatabaseSelectionProvider implements DatabaseSelectionProvider {
	INSTANCE;

	@Override
	public DatabaseSelection getDatabaseSelection() {
		return DatabaseSelection.homeDatabase();
	}
}

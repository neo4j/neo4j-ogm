/*
 * Copyright (c) 2002-2023 "Neo4j,"
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

import java.util.Objects;

public final class UserSelection {

	private static final UserSelection CONNECTED_USER = new UserSelection(null);

	/**
	 * @return A user selection that will just use the user owning the physical connection.
	 */
	public static UserSelection connectedUser() {

		return CONNECTED_USER;
	}

	/**
	 * @param value The name of the user to impersonate
	 * @return A user selection representing an impersonated user.
	 */
	public static UserSelection impersonate(String value) {

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Cannot impersonate user without username");
        }
		return new UserSelection(value);
	}

    private final String value;

	private UserSelection(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		UserSelection that = (UserSelection) o;
		return Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}
}

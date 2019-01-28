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
package org.neo4j.ogm.domain.cineasts.annotated;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * @author Luanne Misquitta
 */
public class SecurityRoleConverter implements AttributeConverter<SecurityRole[], String[]> {

    @Override
    public String[] toGraphProperty(SecurityRole[] value) {
        if (value == null) {
            return null;
        }
        String[] values = new String[(value.length)];
        int i = 0;
        for (SecurityRole securityRole : value) {
            values[i++] = securityRole.name();
        }
        return values;
    }

    @Override
    public SecurityRole[] toEntityAttribute(String[] value) {
        SecurityRole[] roles = new SecurityRole[value.length];
        int i = 0;
        for (String role : value) {
            roles[i++] = SecurityRole.valueOf(role);
        }
        return roles;
    }
}

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
package org.neo4j.ogm.context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.ConvertibleTypes;

/**
 * This class extract values from scalar queries, i.e. queries that only return one column. The name of this class is
 * currently very misleading and should change in the future.
 *
 * @author Vince Bickers
 * @author Jim Webber
 * @author Mark Angrish
 * @author Gerrit Meier
 * @author Michael J. Simons
 * @deprecated since 3.1.1, no replacement yet but expect this mapper to go away.
 */
@Deprecated
public class EntityRowModelMapper implements ResponseMapper<RowModel> {

    private static final Set<Class<?>> VOID_TYPES = new HashSet<>(Arrays.asList(Void.class, void.class));

    /**
     * @param <T> The type of entity to which the row is to be mapped
     */
    @Override
    public <T> Iterable<T> map(Class<T> type, Response<RowModel> response) {

        if (VOID_TYPES.contains(type)) {
            return Collections.emptyList();
        }

        Collection<T> result = new ArrayList<>();
        RowModel model;
        while ((model = response.next()) != null) {
            result.add(extractColumnValue(type, model));
        }
        return result;
    }

    private static <T> T extractColumnValue(Class<T> type, RowModel model) {

        if (model.variables().length > 1) {
            throw new RuntimeException(
                "Scalar response queries must only return one column. Make sure your cypher query only returns one item.");
        }
        final Object o = model.getValues()[0];
        return Optional.ofNullable(ConvertibleTypes.REGISTRY.get(type.getCanonicalName()))
            .map(ac -> (AttributeConverter<T, Object>) (type.isArray() ? ac.forArray : ac.forScalar))
            .map(c -> c.toEntityAttribute(o))
            .orElse((T) Utils.coerceTypes(type, o));
    }
}

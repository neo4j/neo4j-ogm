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
package org.neo4j.ogm.typeconversion;

import java.util.function.Function;

/**
 * This class serves as a holder for converters from a given type to another. The types of the converters must match.
 * They only differ in the way they treat scalars, arrays and iterables.
 *
 * @author Michael J. Simons
 */
public class AttributeConverters {
    public final AttributeConverter<?, ?> forScalar;
    public final AttributeConverter<?, ?> forArray;
    public final Function<String, AttributeConverter<?, ?>> forIterable;

    static class Builder {
        private AttributeConverters buildTarget;

        private Builder(AttributeConverters buildTarget) {
            this.buildTarget = buildTarget;
        }

        static Builder forScalar(AttributeConverter<?, ?> c) {
            return new Builder(new AttributeConverters(c, null, null));
        }

        static AttributeConverters onlyScalar(AttributeConverter<?, ?> c) {
            return forScalar(c).buildTarget;
        }

        static AttributeConverters onlyArray(AttributeConverter<?, ?> c) {
            return forScalar(null).array(c).buildTarget;
        }

        Builder array(final AttributeConverter<?, ?> c) {

            this.buildTarget = new AttributeConverters(this.buildTarget.forScalar, c, null);
            return this;
        }

        AttributeConverters andIterable(Function<String, AttributeConverter<?, ?>> c) {
            return new AttributeConverters(this.buildTarget.forScalar, this.buildTarget.forArray, c);
        }
    }

    private AttributeConverters(AttributeConverter<?, ?> forScalar,
        AttributeConverter<?, ?> forArray, Function<String, AttributeConverter<?, ?>> forIterable) {

        this.forScalar = forScalar;
        this.forArray = forArray;
        this.forIterable = forIterable;
    }
}

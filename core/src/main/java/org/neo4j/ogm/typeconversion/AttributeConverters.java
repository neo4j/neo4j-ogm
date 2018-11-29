/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

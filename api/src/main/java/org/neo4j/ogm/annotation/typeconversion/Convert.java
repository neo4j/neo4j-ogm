/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.annotation.typeconversion;

import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.lang.annotation.*;


/**
 * Annotation to be applied to fields and accessor methods of entity properties to specify the AttributeConverter to use for
 * writing or reading its value in the graph database.
 *
 * @author Vince Bickers
 * @author Adam George
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Convert {

    String CLASS = "org.neo4j.ogm.annotation.typeconversion.Convert";
    String CONVERTER = "value";
    String GRAPH_TYPE = "graphPropertyType";

    /**
     * The type of {@link org.neo4j.ogm.typeconversion.AttributeConverter} implementation to use on this property.
     */
    Class<?> value() default Unset.class;

    /**
     * The type to which the value of the annotated member should be converted before saving as a property in the graph
     * database.
     * <p>
     * This is an optional attribute that should only be needed if {@link #value()} hasn't been set to an explicit
     * converter, as it only gets used to look up converters that can do the job at runtime.
     * </p>
     */
    Class<?> graphPropertyType() default Unset.class;

    /** Placeholder to allow the annotation to be applied without specifying an explicit converter implementation. */
    abstract class Unset implements AttributeConverter<Object, Object> {}

}


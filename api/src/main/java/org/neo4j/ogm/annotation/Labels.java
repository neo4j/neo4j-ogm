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

package org.neo4j.ogm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to declare that the mapped entity will control which labels are added to a node. On save the node's
 * labels will equal the contents of the collection property that this label is applied to, plus the class name or
 * or @NodeEntity label property, if applicable.
 * <p>
 * If this annotation does not exist or refers to a null property on the mapped entity, then the node's labels
 * will not be managed, that is, left as is.
 *
 * @author Jasper Blues
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Labels {

    String CLASS = "org.neo4j.ogm.annotation.Labels";
    String[] defaultValue() default {};
}

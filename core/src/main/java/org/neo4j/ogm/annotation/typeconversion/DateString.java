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
package org.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates OGM to store dates as String in ISO_8601 format in the database.
 * Applicable to `java.util.Date` and `java.time.Instant`
 *
 * @author Vince Bickers
 * @author Gerrit Meier
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface DateString {

    String FORMAT = "value";

    String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    String DEFAULT_ZONE_ID = "UTC";

    String value() default ISO_8601;

    /**
     * Some temporals like {@link java.time.Instant}, representing an instantaneous point in time cannot be formatted
     * with a given {@link java.time.ZoneId}. In case you want to format an instant or similar with a default pattern,
     * we assume a zone with the given id and default to {@literal UTC} which is the same assumption that the predefined
     * patterns in {@link java.time.format.DateTimeFormatter} take.
     * @return The zone id to use when applying a custom pattern to an instant temporal.
     */
    String zoneId() default DEFAULT_ZONE_ID;

    /**
     * Toggle lenient conversion mode by setting this flag to true (defaults to false).
     * Has to be supported by the corresponding converter.
     * @return flag that represents the desired conversion mode.
     */
    boolean lenient() default false;
}


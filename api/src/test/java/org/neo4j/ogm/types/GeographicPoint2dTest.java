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
package org.neo4j.ogm.types;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.types.spatial.GeographicPoint2d;

/**
 * @author Michael J. Simons
 */
public class GeographicPoint2dTest {
    @Test
    public void constructorShouldSetCorrectFields() {

        double latitude = 48.793889;
        double longitude = 9.226944;
        GeographicPoint2d geographicPoint2d = new GeographicPoint2d(latitude, longitude);

        assertThat(geographicPoint2d.getLatitude()).isEqualTo(latitude);
        assertThat(geographicPoint2d.getLongitude()).isEqualTo(longitude);
    }
}

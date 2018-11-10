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

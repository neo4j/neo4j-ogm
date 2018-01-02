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

package org.neo4j.ogm.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import static org.neo4j.ogm.testutil.MultiDriverTestClass.getBaseConfiguration;

/**
 * @author Frantisek Hartman
 */
public class IndexConfigurationTest {

    @Test
    public void shouldPreserveNoneConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("none").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
    }

    @Test
    public void shouldPreserveAssertConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("assert").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.ASSERT);
    }

    @Test
    public void shouldPreserveUpdateConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("update").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.UPDATE);
    }

    @Test
    public void shouldPreserveValidateConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("validate").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.VALIDATE);
    }

    @Test
    public void shouldPreserveDumpConfiguration() {
        Configuration configuration = getBaseConfiguration().autoIndex("dump").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.DUMP);
    }

}

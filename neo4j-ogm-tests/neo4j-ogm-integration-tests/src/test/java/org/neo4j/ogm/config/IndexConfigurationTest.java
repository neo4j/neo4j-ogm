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
package org.neo4j.ogm.config;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 */
public class IndexConfigurationTest extends TestContainersTestBase {

    @Test
    public void shouldPreserveNoneConfiguration() {
        Configuration configuration = getBaseConfigurationBuilder().autoIndex("none").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.NONE);
    }

    @Test
    public void shouldPreserveAssertConfiguration() {
        Configuration configuration = getBaseConfigurationBuilder().autoIndex("assert").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.ASSERT);
    }

    @Test
    public void shouldPreserveUpdateConfiguration() {
        Configuration configuration = getBaseConfigurationBuilder().autoIndex("update").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.UPDATE);
    }

    @Test
    public void shouldPreserveValidateConfiguration() {
        Configuration configuration = getBaseConfigurationBuilder().autoIndex("validate").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.VALIDATE);
    }

    @Test
    public void shouldPreserveDumpConfiguration() {
        Configuration configuration = getBaseConfigurationBuilder().autoIndex("dump").build();
        assertThat(configuration.getAutoIndex()).isEqualTo(AutoIndexMode.DUMP);
    }

}

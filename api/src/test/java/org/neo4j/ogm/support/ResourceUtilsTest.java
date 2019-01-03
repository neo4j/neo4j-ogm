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
package org.neo4j.ogm.support;

import static org.assertj.core.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

/**
 * @author Michael J. Simons
 */
public class ResourceUtilsTest {

    @Test
    public void protocollessUrlsShouldBeTreatedAsClasspathUrls() throws FileNotFoundException {
        URL url = ResourceUtils.getResourceUrl("some.properties");
        assertThat(url).isNotNull();
    }

    @Test
    public void classpathUrlsShouldWork() throws FileNotFoundException {
        URL url = ResourceUtils.getResourceUrl("classpath:some.properties");
        assertThat(url).isNotNull();
    }

    @Test
    public void fileUrlsShouldWork() throws IOException {

        Path path = Files.createTempFile("ResourceUtilsTest", ".properties");
        URL url = ResourceUtils.getResourceUrl(path.toUri().toURL().toString());
        assertThat(url).isNotNull();
    }

    @Test
    public void shouldThrowFileNotFoundOnInvalidUrls() {

        assertThatExceptionOfType(FileNotFoundException.class)
            .isThrownBy(() -> ResourceUtils.getResourceUrl("invalid://test.com"));
    }

    @Test
    public void shouldThrowFileNotFoundOnInvalidResources() {

        assertThatExceptionOfType(FileNotFoundException.class)
            .isThrownBy(() -> ResourceUtils.getResourceUrl("idontexists"));
    }
}

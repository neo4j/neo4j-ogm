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

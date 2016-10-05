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

package org.neo4j.ogm.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.neo4j.ogm.metadata.ClassFileProcessor;
import org.neo4j.ogm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class ClassPathScanner {

    private List<String> classPaths;
    private ClassFileProcessor processor;

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathScanner.class);


    private void scanFile(File file, String relativePath) throws IOException {
        if (relativePath.endsWith(".class")) {
            try (InputStream inputStream = new FileInputStream(file)) {
                processor.process(inputStream);
            }
        }
    }

    private void scanFolder(File folder, int prefixSize) throws IOException {

        String absolutePath = folder.getPath();
        String relativePath = prefixSize > absolutePath.length() ? "" : absolutePath.substring(prefixSize);

        File[] subFiles = folder.listFiles();
        if (subFiles != null) {
            for (final File subFile : subFiles) {
                if (subFile.isDirectory()) {
                    scanFolder(subFile, prefixSize);
                } else if (subFile.isFile()) {
                    String leafSuffix = "/" + subFile.getName();
                    scanFile(subFile, relativePath + leafSuffix);
                }
            }
        }
    }


    private void scanZipFile(final ZipFile zipFile) throws IOException {
        LOGGER.debug("Scanning zipFile {}", zipFile.getName());
        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
            final ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                try (InputStream inputStream = zipFile.getInputStream(entry) ) {
                    if (entry.getName().endsWith(".class")) {
                        scanClassFileEntry(inputStream, entry);
                    } else if (isArchive(entry.getName())) {
                        scanZippedEntry(inputStream, entry);
                    }
                }
            }
        }
    }

    private void scanClassFileEntry(InputStream inputStream, ZipEntry entry) throws IOException {

        String name = entry.getName();

        LOGGER.debug("Scanning class entry: {}", name);

        for (String pathToScan : classPaths) {
            if (name.contains(pathToScan)) {
                LOGGER.debug("{} found in {}", pathToScan, name);
                processor.process(inputStream);
                break;
            }
        }
    }

    private void scanZippedEntry(InputStream inputStream, ZipEntry entry) throws IOException {

        String name = entry.getName();

        LOGGER.debug("Scanning zipped entry: {}", name);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null) {
            if (!zipEntry.isDirectory()) {
                if (zipEntry.getName().endsWith(".class")) {
                    scanClassFileEntry(zipInputStream, zipEntry);
                } else if (isArchive(zipEntry.getName())) {
                    scanZippedEntry(zipInputStream, zipEntry);
                }
            }
            zipEntry = zipInputStream.getNextEntry();
        }
    }

    public void scan(List<String> classPaths, ClassFileProcessor processor) {

        this.classPaths = classPaths;
        this.processor = processor;

        Set<File> classPathElements = getUniqueClasspathElements(classPaths);

        LOGGER.debug("Classpath elements:");
        for (File classPathElement : classPathElements) {
            LOGGER.debug(classPathElement.getPath());
        }

        try {
            for (File classPathElement : classPathElements) {
                String path = classPathElement.getPath();
                if (classPathElement.isDirectory()) {
                    scanFolder(classPathElement, path.length() + 1);
                } else if (classPathElement.isFile()) {
                    if (isArchive(path)) {
                        scanZipFile(new ZipFile(classPathElement));
                    } else {
                        scanFile(classPathElement, classPathElement.getName());
                    }
                }
            }
            processor.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Set<File> getUniqueClasspathElements(List<String> classPaths) {
        return ClassUtils.getUniqueClasspathElements(classPaths);
    }

    private boolean isArchive(String s) {
        s = s.toLowerCase();
        return (s.endsWith(".jar") || s.endsWith(".zip") || s.endsWith(".war"));
    }
}

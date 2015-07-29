/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.metadata;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.metadata.info.ClassFileProcessor;

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
        LOGGER.debug("Scanning zipFile " + zipFile.getName());
        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
            final ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                InputStream inputStream = zipFile.getInputStream(entry);
                if (entry.getName().endsWith(".class")) {
                    scanClassFileEntry(inputStream, entry);
                } else if (entry.getName().endsWith(".jar") || entry.getName().endsWith(".zip")) {
                    scanZippedEntry(inputStream, entry);
                }
            }
        }
    }

    private void scanClassFileEntry(InputStream inputStream, ZipEntry entry) throws IOException {

        String name = entry.getName();

        LOGGER.debug("Scanning class entry: " + name);
        int i = name.lastIndexOf("/");
        String path = (i == -1) ? "" : name.substring(0, i);

        for (String pathToScan : classPaths) {
            if (path.equals(pathToScan) || path.startsWith(pathToScan.concat("/"))) {
                LOGGER.debug(pathToScan + " admits '" + path + "' for entry: " + name);
                processor.process(inputStream);
                break;
            }
        }
    }

    private void scanZippedEntry(InputStream inputStream, ZipEntry entry) throws IOException {

        String name = entry.getName();

        LOGGER.debug("Scanning zipped entry: " + name);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);

        ZipEntry zipEntry = zipInputStream.getNextEntry();

        while (zipEntry != null) {
            if (!zipEntry.isDirectory()) {
                if (zipEntry.getName().endsWith(".class")) {
                    scanClassFileEntry(zipInputStream, zipEntry);
                } else if (zipEntry.getName().endsWith(".jar") || zipEntry.getName().endsWith(".zip")) {
                    scanZippedEntry(zipInputStream, zipEntry);
                }
            }
            zipEntry = zipInputStream.getNextEntry();
        }
    }
//

//    private void scanZipFile(final ZipFile zipFile) throws IOException {
//        LOGGER.debug("Scanning " + zipFile.getName());
//        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
//            final ZipEntry entry = entries.nextElement();
//            scanZipEntry(entry, zipFile, null);
//        }
//    }

//  /  private void scanZipEntry(ZipEntry zipEntry, ZipFile zipFile, ZipInputStream zipInputStream) throws IOException {
        
//        if (zipEntry.isDirectory()) {
//           return;
//        }

//        LOGGER.debug("Scanning entry " + zipEntry.getName());
        
//        String zipEntryName = zipEntry.getName();

//        int i = zipEntryName.lastIndexOf("/");
        
//        String path = (i == -1) ? "" : zipEntryName.substring(0, i);

//        if (zipEntryName.endsWith(".jar") || zipEntryName.endsWith(".zip")) { //The zipFile contains a zip or jar
//            InputStream inputStream = zipFile.getInputStream(zipEntry); //Attempt to read the nested zip
//            if (inputStream != null) {
//                ZipInputStream embeddedZipInputStream = new ZipInputStream(inputStream);
//                ZipEntry entry = embeddedZipInputStream.getNextEntry();
//                while (entry != null) { //Recursively scan each entry in the nested zip given its ZipInputStream
//                    scanZipEntry(entry, zipFile, embeddedZipInputStream);
//                    entry = embeddedZipInputStream.getNextEntry();
//                }
//            }
//            else {
//                LOGGER.info("Unable to scan " + zipEntry.getName());
//            }
//            return;
//        }

//        if (zipEntryName.endsWith(".jar") || zipEntryName.endsWith(".zip")) { //The zipFile contains a zip or jar
//            InputStream inputStream = zipFile.getInputStream(zipEntry); //Attempt to read the nested zip
//            if (inputStream != null) {
//                zipInputStream = new ZipInputStream(inputStream);
//            }
//            else {
//                LOGGER.info("Unable to scan " + zipEntry.getName());
//            }
//            ZipEntry entry = zipInputStream.getNextEntry();
//            String nestedPath = null;
//            while (entry != null) { //Recursively scan each entry in the nested zip given its ZipInputStream
//                scanZipEntry(entry, zipFile, zipInputStream);
//                entry = zipInputStream.getNextEntry();
//            }
//        }

//        boolean scanFile = false;

//        for (String pathToScan : classPaths) {
//            if (path.equals(pathToScan) || path.startsWith(pathToScan.concat("/"))) {
//                LOGGER.debug(pathToScan + " admits '" + path + "' for entry: " + zipEntryName);
//                scanFile = true;
//                break;
//            }
//        }

//        if (scanFile && zipEntryName.endsWith(".class")) {
//            if (zipInputStream == null) { //ZipEntry directly in the top level ZipFile
//                try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
//                    processor.process(inputStream);
//                }
//            } else { //Nested ZipEntry, read from its ZipInputStream
//                processor.process(zipInputStream);
//                zipInputStream.closeEntry();
//            }
//        }
//    }

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
                    String pathLower = path.toLowerCase();
                    if (pathLower.endsWith(".jar") || pathLower.endsWith(".zip")) {
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

}

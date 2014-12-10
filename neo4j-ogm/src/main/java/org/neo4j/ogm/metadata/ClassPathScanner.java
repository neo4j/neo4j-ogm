package org.neo4j.ogm.metadata;

import org.neo4j.ogm.metadata.info.ClassInfoProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassPathScanner {

    private List<String> classPaths;
    private ClassInfoProcessor processor;

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

        boolean scanFolders = false, scanFiles = false;

        // TODO: use filter pattern
        for (String pathToScan : classPaths) {
            if (relativePath.startsWith(pathToScan) || (relativePath.length() == pathToScan.length() - 1 && pathToScan.startsWith(relativePath))) {
                scanFolders = scanFiles = true;
                break;
            }
            if (pathToScan.startsWith(relativePath)) {
                scanFolders = true;
            }
        }

        if (scanFolders || scanFiles) {
            File[] subFiles = folder.listFiles();
            if (subFiles != null) {
                for (final File subFile : subFiles) {
                    if (subFile.isDirectory()) {
                        scanFolder(subFile, prefixSize);
                    } else if (scanFiles && subFile.isFile()) {
                        String leafSuffix = "/" + subFile.getName();
                        scanFile(subFile, relativePath + leafSuffix);
                    }
                }
            }
        }
    }

    /**
     * Scan a zipfile for matching file path patterns. (Does not recurse into zipfiles within zipfiles.)
     */
    private void scanZipFile(final ZipFile zipFile) throws IOException {

        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
            final ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                String path = entry.getName();
                boolean scanFile = false;
                for (String pathToScan : classPaths) {
                    if (path.startsWith(pathToScan)) {
                        scanFile = true;
                        break;
                    }
                }
                if (scanFile && path.endsWith(".class")) {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        processor.process(inputStream);
                    }
                }
            }
        }
    }

    public void scan(List<String> classPaths, ClassInfoProcessor processor) {

        this.classPaths = classPaths;
        this.processor = processor;

        try {
            for (File pathElt : ClassUtils.getUniqueClasspathElements()) {
                String path = pathElt.getPath();
                if (pathElt.isDirectory()) {
                    scanFolder(pathElt, path.length() + 1);
                } else if (pathElt.isFile()) {
                    String pathLower = path.toLowerCase();
                    if (pathLower.endsWith(".jar") || pathLower.endsWith(".zip")) {
                        scanZipFile(new ZipFile(pathElt));
                    } else {
                        scanFile(pathElt, pathElt.getName());
                    }
                }
            }
            processor.finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

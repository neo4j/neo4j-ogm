package org.neo4j.ogm.metadata.dictionary;

import org.neo4j.ogm.metadata.MappingException;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A Type Hierarchy (including Interfaces) is actually a DAG. Maybe we should be using Neo? !!
 *
 * This class needs a lot of tidying up
 */
public class Classify {

    private List<String> classPath = new ArrayList<>();

    private final HashMap<String, ClassInfo> classNameToClassInfo = new HashMap<>();
    private final HashMap<String, InterfaceInfo> interfaceNameToInterfaceInfo = new HashMap<>();
    private final HashMap<String, ArrayList<String>> annotationToClasses = new HashMap<>();
    private final HashMap<String, ArrayList<String>> interfaceToClasses = new HashMap<>();

    private static void buildTree(ClassInfo curr) {
        for (ClassInfo subclass : curr.directSubclasses) {
            buildTree(subclass);
        }
    }

    private void constructInterfaceHierarcy(InterfaceInfo interfaceInfo) {
        if (interfaceInfo.allSuperInterfaces.isEmpty() && !interfaceInfo.superInterfaces.isEmpty()) {
            interfaceInfo.allSuperInterfaces.addAll(interfaceInfo.superInterfaces);
            for (String interfaceName : interfaceInfo.superInterfaces) {
                InterfaceInfo superinterfaceInfo = interfaceNameToInterfaceInfo.get(interfaceName);
                if (superinterfaceInfo != null) {
                    constructInterfaceHierarcy(superinterfaceInfo);
                    interfaceInfo.allSuperInterfaces.addAll(superinterfaceInfo.allSuperInterfaces);
                }
            }
        }
    }

    private void constructClassHierarchy() {

        if (classNameToClassInfo.isEmpty() && interfaceNameToInterfaceInfo.isEmpty()) {
            return;
        }

        /*
         * get the root classes in the type hierarchy.
         */
        ArrayList<ClassInfo> roots = new ArrayList<>();
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            if (classInfo.directSuperclass == null) {
                roots.add(classInfo);
            }
        }

        LinkedList<ClassInfo> nodes = new LinkedList<>();
        nodes.addAll(roots);
        while (!nodes.isEmpty()) {
            ClassInfo head = nodes.removeFirst();
            for (ClassInfo subclass : head.directSubclasses) {
                nodes.add(subclass);
            }
        }

        for (ClassInfo root : roots) {
            buildTree(root);
        }

        // A <-[:has_annotation]- T
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            for (String annotation : classInfo.annotations) {
                ArrayList<String> classList = annotationToClasses.get(annotation);
                if (classList == null) {
                    annotationToClasses.put(annotation, classList = new ArrayList<>());
                }
                classList.add(classInfo.name);
            }
        }

        // I - [:extends] -> J
        for (InterfaceInfo interfaceInfo : interfaceNameToInterfaceInfo.values()) {
            constructInterfaceHierarcy(interfaceInfo);
        }

        // S -[:implements]-> I
        for (ClassInfo classInfo : classNameToClassInfo.values()) {
            HashSet<String> interfaceAndSuperinterfaces = new HashSet<>();
            for (String interfaceName : classInfo.interfaces) {
                interfaceAndSuperinterfaces.add(interfaceName);
                InterfaceInfo interfaceInfo = interfaceNameToInterfaceInfo.get(interfaceName);
                if (interfaceInfo != null) {
                    interfaceAndSuperinterfaces.addAll(interfaceInfo.allSuperInterfaces);
                }
            }
            for (String interfaceName : interfaceAndSuperinterfaces) {
                ArrayList<String> classList = interfaceToClasses.get(interfaceName);
                if (classList == null) {
                    interfaceToClasses.put(interfaceName, classList = new ArrayList<>());
                }
                classList.add(classInfo.name);
            }
        }

        // transitive interface implementations: S-[:extends]->T-[:implements]->I  => S-[:implements]->I
        for (String interfaceName : interfaceToClasses.keySet()) {
            ArrayList<String> classes = interfaceToClasses.get(interfaceName);
            HashSet<String> subClasses = new HashSet<>(classes);
            for (String klass : classes) {
                ClassInfo classInfo = classNameToClassInfo.get(klass);
                if (classInfo != null) {
                    for (ClassInfo subClassInfo : classInfo.directSubclasses) {
                        subClasses.add(subClassInfo.name);
                    }
                }
            }
            interfaceToClasses.put(interfaceName, new ArrayList<>(subClasses));
        }
    }

    private String readAnnotation(final DataInputStream dataInputStream, Object[] constantPool) throws IOException {
        String annotationFieldDescriptor = lookup(dataInputStream, constantPool);
        String annotationClassName;
        if (annotationFieldDescriptor.charAt(0) == 'L'
                && annotationFieldDescriptor.charAt(annotationFieldDescriptor.length() - 1) == ';') {
            // Lcom/xyz/Annotation; -> com.xyz.Annotation
            annotationClassName = annotationFieldDescriptor.substring(1,
                    annotationFieldDescriptor.length() - 1).replace('/', '.');
        } else {
            // Should not happen
            annotationClassName = annotationFieldDescriptor;
        }
        int numElementValuePairs = dataInputStream.readUnsignedShort();
        for (int i = 0; i < numElementValuePairs; i++) {
            dataInputStream.skipBytes(2); // element_name_index
            readAnnotationElementValue(dataInputStream, constantPool);
        }
        return annotationClassName;
    }

    private void readAnnotationElementValue(final DataInputStream dataInputStream, Object[] constantPool) throws IOException {
        int tag = dataInputStream.readUnsignedByte();
        switch (tag) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 's':
                // const_value_index
                dataInputStream.skipBytes(2);
                break;
            case 'e':
                // enum_const_value
                dataInputStream.skipBytes(4);
                break;
            case 'c':
                // class_info_index
                dataInputStream.skipBytes(2);
                break;
            case '@':
                // Complex (nested) annotation
                readAnnotation(dataInputStream, constantPool);
                break;
            case '[':
                // array_value
                final int count = dataInputStream.readUnsignedShort();
                for (int l = 0; l < count; ++l) {
                    // Nested annotation element value
                    readAnnotationElementValue(dataInputStream, constantPool);
                }
                break;
            default:
                throw new ClassFormatError("Invalid annotation element type tag: 0x" + Integer.toHexString(tag));
        }
    }

    /**
     * Read a string reference from a class file, then look up the string in the constant pool.
     */
    private static String lookup(DataInputStream dataInputStream, Object[] constantPool) throws IOException {
        int constantPoolIdx = dataInputStream.readUnsignedShort();
        Object constantPoolObj = constantPool[constantPoolIdx];
        return (constantPoolObj instanceof Integer
                ? (String) constantPool[(Integer) constantPoolObj]
                : (String) constantPoolObj);
    }

    /**
     * Directly examine contents of class file binary header.
     */
    private void readClassInfo(final InputStream inputStream) throws IOException {

        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(inputStream, 1024));

        // Magic
        if (dataInputStream.readInt() != 0xCAFEBABE) {
            return;
        }

        dataInputStream.readUnsignedShort();    //minor version
        dataInputStream.readUnsignedShort();    // major version

        // Constant pool count (1-indexed, zeroth entry not used)
        int cpCount = dataInputStream.readUnsignedShort();

        // Constant pool
        Object[] constantPool = new Object[cpCount];
        for (int i = 1; i < cpCount; ++i) {
            final int tag = dataInputStream.readUnsignedByte();
            switch (tag) {
                case 1: // Modified UTF8
                    constantPool[i] = dataInputStream.readUTF();
                    break;
                case 3: // int
                case 4: // float
                    dataInputStream.skipBytes(4);
                    break;
                case 5: // long
                case 6: // double
                    dataInputStream.skipBytes(8);
                    i++; // double slot
                    break;
                case 7: // Class
                case 8: // String
                    // Forward or backward reference a Modified UTF8 entry
                    constantPool[i] = dataInputStream.readUnsignedShort();
                    break;
                case 9: // field ref
                case 10: // method ref
                case 11: // interface ref
                case 12: // name and type
                    dataInputStream.skipBytes(4); // two shorts
                    break;
                case 15: // method handle
                    dataInputStream.skipBytes(3);
                    break;
                case 16: // method type
                    dataInputStream.skipBytes(2);
                    break;
                case 18: // invoke dynamic
                    dataInputStream.skipBytes(4);
                    break;
                default:
                    throw new ClassFormatError("Unkown tag value for constant pool entry: " + tag);
            }
        }

        // Access flags
        int flags = dataInputStream.readUnsignedShort();
        boolean isInterface = (flags & 0x0200) != 0;

        String className = lookup(dataInputStream, constantPool).replace('/', '.');
        String superclassName = lookup(dataInputStream, constantPool).replace('/', '.');

        // Interfaces
        int interfaceCount = dataInputStream.readUnsignedShort();
        ArrayList<String> interfaces = new ArrayList<>();
        for (int i = 0; i < interfaceCount; i++) {
            interfaces.add(lookup(dataInputStream, constantPool).replace('/', '.'));
        }

        // Fields
        int fieldCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < fieldCount; i++) {
            dataInputStream.skipBytes(6); // access_flags, name_index, descriptor_index
            int attributesCount = dataInputStream.readUnsignedShort();

            for (int j = 0; j < attributesCount; j++) {
                dataInputStream.skipBytes(2); // attribute_name_index
                int attributeLength = dataInputStream.readInt();
                dataInputStream.skipBytes(attributeLength);
            }
        }

        // Methods
        int methodCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < methodCount; i++) {
            dataInputStream.skipBytes(6); // access_flags, name_index, descriptor_index
            int attributesCount = dataInputStream.readUnsignedShort();
            for (int j = 0; j < attributesCount; j++) {
                dataInputStream.skipBytes(2); // attribute_name_index
                int attributeLength = dataInputStream.readInt();
                dataInputStream.skipBytes(attributeLength);
            }
        }

        // Annotations
        HashSet<String> annotations = new HashSet<>();
        int attributesCount = dataInputStream.readUnsignedShort();
        for (int i = 0; i < attributesCount; i++) {
            String attributeName = lookup(dataInputStream, constantPool);
            int attributeLength = dataInputStream.readInt();
            if ("RuntimeVisibleAnnotations".equals(attributeName)) {
                int annotationCount = dataInputStream.readUnsignedShort();
                for (int m = 0; m < annotationCount; m++) {
                    String annotationName = readAnnotation(dataInputStream, constantPool);
                    annotations.add(annotationName);
                }
            }
            else {
                dataInputStream.skipBytes(attributeLength);
            }
        }

        // split reader here, and return the interfaces and annotations ?

        if (isInterface) {
            // its an interface ref
            InterfaceInfo thisInterfaceInfo = interfaceNameToInterfaceInfo.get(className);
            if (thisInterfaceInfo == null) {
                interfaceNameToInterfaceInfo.put(className, new InterfaceInfo(interfaces));
            } else {
                return;
            }

        } else {
            // its a class ref
            ClassInfo thisClassInfo = classNameToClassInfo.get(className);
            if (thisClassInfo == null) {
                thisClassInfo = new ClassInfo(className, interfaces, annotations);
                classNameToClassInfo.put(className, thisClassInfo);
            } else if (thisClassInfo.visited) {
                return;
            } else {
                thisClassInfo.visit(interfaces, annotations);
            }

            ClassInfo superclassInfo = classNameToClassInfo.get(superclassName);
            if (superclassInfo == null) {
                classNameToClassInfo.put(superclassName, new ClassInfo(superclassName, thisClassInfo));
            } else {
                superclassInfo.addSubclass(thisClassInfo);
            }
        }

    }

    private void scanFile(File file, String relativePath) throws IOException {
        if (relativePath.endsWith(".class")) {
            try (InputStream inputStream = new FileInputStream(file)) {
                readClassInfo(inputStream);
            }
        }
    }

    private void scanFolder(File folder, int prefixSize) throws IOException {

        String absolutePath = folder.getPath();
        String relativePath = prefixSize > absolutePath.length() ? "" : absolutePath.substring(prefixSize);

        boolean scanFolders = false, scanFiles = false;

        // TODO: use filter pattern
        for (String pathToScan : classPath) {
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

    /**
     * Scan a zipfile for matching file path patterns. (Does not recurse into zipfiles within zipfiles.)
     */
    private void scanZipFile(final ZipFile zipFile) throws IOException {

        for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
            final ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                String path = entry.getName();
                boolean scanFile = false;
                for (String pathToScan : classPath) {
                    if (path.startsWith(pathToScan)) {
                        scanFile = true;
                        break;
                    }
                }
                if (scanFile && path.endsWith(".class")) {
                    try (InputStream inputStream = zipFile.getInputStream(entry)) {
                        readClassInfo(inputStream);
                    }
                }
            }
        }
    }

    /**
     * Get a list of unique elements on the classpath as File objects, preserving order.
     * Classpath elements that do not exist are not returned.
     */
    public static ArrayList<File> getUniqueClasspathElements() {
        String[] pathElements = System.getProperty("java.class.path").split(File.pathSeparator);
        HashSet<String> pathElementsSet = new HashSet<>();
        ArrayList<File> pathFiles = new ArrayList<>();
        for (String pathElement : pathElements) {
            if (pathElementsSet.add(pathElement)) {
                File file = new File(pathElement);
                if (file.exists()) {
                    pathFiles.add(file);
                }
            }
        }
        return pathFiles;
    }

    public void scan(String... packages) {

        classPath.clear();
        classNameToClassInfo.clear();
        interfaceNameToInterfaceInfo.clear();
        annotationToClasses.clear();
        interfaceToClasses.clear();

        for (String packageName : packages) {
            String path = packageName.replaceAll("\\.", File.separator);
            classPath.add(path);
        }

        try {
            for (File pathElt : getUniqueClasspathElements()) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        constructClassHierarchy();

    }

    public ClassInfo getClass(String fqn) {
        return classNameToClassInfo.get(fqn);
    }

    public ClassInfo getClassSimpleName(String simpleClassName) {

        ClassInfo match = null;
        for (String fqn : classNameToClassInfo.keySet()) {
            if (fqn.endsWith("." + simpleClassName)) {
                if (match == null) {
                    match = classNameToClassInfo.get(fqn);
                } else {
                    throw new MappingException("More than one class has simple name: " + simpleClassName);
                }
            }
        }
        return match;
    }

    public ClassInfo getNamedClassWithAnnotation(String annotation, String className) {
        for (String fqn : annotationToClasses.get(annotation)) {
            if (fqn.equals(className)) {
                return getClass(fqn);
            }
        }
        return null;
    }

    public List<String> getFQNsWithAnnotation(String annotation) {
        return annotationToClasses.get(annotation);
    }
}

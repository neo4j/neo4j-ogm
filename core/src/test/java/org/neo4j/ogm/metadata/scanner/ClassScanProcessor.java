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

package org.neo4j.ogm.metadata.scanner;

import org.neo4j.ogm.metadata.ClassFileProcessor;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.bytecode.ClassInfoBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ClassFileProcessor} that exists solely to record information about classes scanned for test purposes.
 *
 * @author Luanne Misquitta
 */
public class ClassScanProcessor implements ClassFileProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassScanProcessor.class);

    List<ClassInfo> domainClassInfos = new ArrayList<>();

    @Override
    public void process(InputStream inputStream) throws IOException {
        ClassInfo classInfo = ClassInfoBuilder.create(inputStream);
        domainClassInfos.add(classInfo);
        LOGGER.debug("ClassScanProcessor added: {}", classInfo.name());
    }

    @Override
    public void finish() {
        LOGGER.debug("ClassScanProcessor loaded:");
        for (ClassInfo classInfo : domainClassInfos) {
            LOGGER.debug(classInfo.name());
        }
    }
}

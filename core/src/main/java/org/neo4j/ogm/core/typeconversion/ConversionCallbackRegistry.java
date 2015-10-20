/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and licence terms.  Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's licence, as noted in the LICENSE file.
 */
package org.neo4j.ogm.core.typeconversion;

/**
 * Let's do the simplest thing we possibly can to link this together.
 * This class is not thread-safe.
 */
public class ConversionCallbackRegistry {

    private ConversionCallback conversionCallback;

    public ConversionCallback lookUpConverter() {
        return this.conversionCallback;
    }

    public void registerConversionCallback(ConversionCallback conversionCallback) {
        this.conversionCallback = conversionCallback;
    }

}

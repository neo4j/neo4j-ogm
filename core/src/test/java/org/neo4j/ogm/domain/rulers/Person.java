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

package org.neo4j.ogm.domain.rulers;

import java.util.List;

/**
 * @author Vince Bickers
 */
public abstract class Person {

    protected List<Person> heirs;

    protected String name;

    public abstract String sex();

    public abstract boolean isCommoner();

    public List<Person> getHeirs() {
        return heirs;
    }

    public void setHeirs(List<Person> heirs) {
        this.heirs = heirs;
    }

}

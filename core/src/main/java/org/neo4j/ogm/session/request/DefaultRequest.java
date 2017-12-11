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

package org.neo4j.ogm.session.request;

import java.util.Collections;
import java.util.List;

import javax.swing.plaf.nimbus.State;

import org.neo4j.ogm.request.Statement;

/**
 * @author Luanne Misquitta
 */
public class DefaultRequest implements org.neo4j.ogm.request.DefaultRequest {

    List<Statement> statements;

    public DefaultRequest() {
    }

    public DefaultRequest(Statement statement) {
        this.statements = Collections.singletonList(statement);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }
}

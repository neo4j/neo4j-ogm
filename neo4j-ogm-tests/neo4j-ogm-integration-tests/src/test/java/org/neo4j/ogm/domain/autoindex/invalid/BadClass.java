/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.domain.autoindex.invalid;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;

/**
 * @author Mark Angrish
 */
public class BadClass {

    @Id @GeneratedValue
    private Long id;

    @Id
    private Long number;

    @Id
    private String companyEmail;

    private Long amountInCents;

    public BadClass() {
    }

    public BadClass(Long number, String companyEmail, Long amountInCents) {
        this.number = number;
        this.companyEmail = companyEmail;
        this.amountInCents = amountInCents;
    }

    public Long getId() {
        return id;
    }

    public Long getNumber() {
        return number;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public Long getAmountInCents() {
        return amountInCents;
    }
}

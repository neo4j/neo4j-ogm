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
package org.neo4j.ogm.domain.autoindex.valid;

import java.util.Objects;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

/**
 * @author Mark Angrish
 * @author Michael J. Simons
 */
@NodeEntity
public class Invoice {

    @Id
    @Property(name = "invoice_number")
    private Long number;

    @Index
    @Property(name = "company_id")
    private String company;

    private Long amountInCents;

    public Invoice() {
    }

    public Invoice(Long number, String company, Long amountInCents) {
        this.number = number;
        this.company = company;
        this.amountInCents = amountInCents;
    }

    public Long getNumber() {
        return number;
    }

    public String getCompany() {
        return company;
    }

    public Long getAmountInCents() {
        return amountInCents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Invoice))
            return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(number, invoice.number);
    }

    @Override public int hashCode() {
        return Objects.hash(number);
    }
}

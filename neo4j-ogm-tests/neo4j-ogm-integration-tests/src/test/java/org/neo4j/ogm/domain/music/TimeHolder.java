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
package org.neo4j.ogm.domain.music;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * Holder for various temporal objects that are all subject to XXXStringConverter.
 *
 * @author Michael J. Simons
 */
@NodeEntity(label = "Data")
public class TimeHolder {

    @Id
    @GeneratedValue
    private Long graphId;

    private OffsetDateTime someTime;

    private LocalDateTime someLocalDateTime;

    private LocalDate someLocalDate;

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
    }

    public OffsetDateTime getSomeTime() {
        return someTime;
    }

    public void setSomeTime(OffsetDateTime someTime) {
        this.someTime = someTime;
    }

    public LocalDateTime getSomeLocalDateTime() {
        return someLocalDateTime;
    }

    public void setSomeLocalDateTime(LocalDateTime someLocalDateTime) {
        this.someLocalDateTime = someLocalDateTime;
    }

    public LocalDate getSomeLocalDate() {
        return someLocalDate;
    }

    public void setSomeLocalDate(LocalDate someLocalDate) {
        this.someLocalDate = someLocalDate;
    }
}

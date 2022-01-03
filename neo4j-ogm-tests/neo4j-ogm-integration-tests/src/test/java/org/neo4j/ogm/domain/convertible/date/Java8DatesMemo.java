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
package org.neo4j.ogm.domain.convertible.date;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.neo4j.ogm.annotation.typeconversion.DateLong;

/**
 * @author Nicolas Mervaillie
 */
public class Java8DatesMemo {

    private Long id;
    private String memo;

    private Instant recorded;

    @DateLong
    private Instant closed;

    private LocalDate approved;

    private List<LocalDate> dateList;

    private LocalDateTime dateTime;

    private List<LocalDateTime> dateTimeList;

    private OffsetDateTime offsetDateTime;

    private List<OffsetDateTime> offsetDateTimeList;

    public Java8DatesMemo() {
    }

    public Java8DatesMemo(Instant initial) {
        this.recorded = initial;
        this.closed = initial;
        this.approved = LocalDateTime.ofInstant(initial, ZoneOffset.UTC).toLocalDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Instant getRecorded() {
        return recorded;
    }

    public void setRecorded(Instant recorded) {
        this.recorded = recorded;
    }

    public LocalDate getApproved() {
        return approved;
    }

    public void setApproved(LocalDate approved) {
        this.approved = approved;
    }

    public Instant getClosed() {
        return closed;
    }

    public void setClosed(Instant closed) {
        this.closed = closed;
    }

    public List<LocalDate> getDateList() {
        return dateList;
    }

    public void setDateList(List<LocalDate> dateList) {
        this.dateList = dateList;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public List<LocalDateTime> getDateTimeList() {
        return dateTimeList;
    }

    public void setDateTimeList(List<LocalDateTime> dateTimeList) {
        this.dateTimeList = dateTimeList;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public List<OffsetDateTime> getOffsetDateTimeList() {
        return offsetDateTimeList;
    }

    public void setOffsetDateTimeList(List<OffsetDateTime> offsetDateTimeList) {
        this.offsetDateTimeList = offsetDateTimeList;
    }
}

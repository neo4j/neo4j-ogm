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
import java.util.Date;
import java.util.Set;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.neo4j.ogm.annotation.typeconversion.DateString;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Gerrit Meier
 */
public class Memo {

    private Long id;
    private String memo;

    // uses default ISO 8601 date format
    private Date recorded;

    // declares a custom converter
    @Convert(DateNumericStringConverter.class)
    private Date approved;

    @DateString("yyyy-MM-dd")
    private Date actioned;

    @DateString
    private Date modified;

    @DateString(lenient = true)
    private Date legacyDate;

    @DateLong
    private Date closed;

    @DateString
    private Instant actionedAsInstant;

    @DateString(value = "yyyy-MM-dd HH:mm:ss")
    private Instant actionedAsInstantWithCustomFormat1;

    @DateString(value = "yyyy-MM-dd HH:mm:ss", zoneId = "Europe/Berlin")
    private Instant actionedAsInstantWithCustomFormat2;

    // uses default ISO 8601 date format
    private Date[] escalations;

    // uses default ISO 8601 date format
    private Set<Date> implementations;

    public Memo() {
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

    public Date getRecorded() {
        return recorded;
    }

    public void setRecorded(Date recorded) {
        this.recorded = recorded;
    }

    public Date getActioned() {
        return actioned;
    }

    public void setActioned(Date actioned) {
        this.actioned = actioned;
    }

    public Instant getActionedAsInstant() {
        return actionedAsInstant;
    }

    public void setActionedAsInstant(Instant actionedAsInstant) {
        this.actionedAsInstant = actionedAsInstant;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public Date getLegacyDate() {
        return legacyDate;
    }

    public void setLegacyDate(Date legacyDate) {
        this.legacyDate = legacyDate;
    }

    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    public Date getApproved() {
        return approved;
    }

    public void setApproved(Date approved) {
        this.approved = approved;
    }

    public Date[] getEscalations() {
        return escalations;
    }

    public void setEscalations(Date[] escalations) {
        this.escalations = escalations;
    }

    public Set<Date> getImplementations() {
        return implementations;
    }

    public void setImplementations(Set<Date> implementations) {
        this.implementations = implementations;
    }

    public Instant getActionedAsInstantWithCustomFormat1() {
        return actionedAsInstantWithCustomFormat1;
    }

    public void setActionedAsInstantWithCustomFormat1(Instant actionedAsInstantWithCustomFormat1) {
        this.actionedAsInstantWithCustomFormat1 = actionedAsInstantWithCustomFormat1;
    }

    public Instant getActionedAsInstantWithCustomFormat2() {
        return actionedAsInstantWithCustomFormat2;
    }

    public void setActionedAsInstantWithCustomFormat2(Instant actionedAsInstantWithCustomFormat2) {
        this.actionedAsInstantWithCustomFormat2 = actionedAsInstantWithCustomFormat2;
    }
}

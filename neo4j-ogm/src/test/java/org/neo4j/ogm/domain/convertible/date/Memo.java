/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.domain.convertible.date;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.neo4j.ogm.annotation.typeconversion.DateString;

import java.util.Date;

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

    @DateLong
    private Date closed;

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

    @DateString("yyyy-MM-dd")
    public void setActioned(Date actioned) {
        this.actioned = actioned;
    }

    @DateLong
    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    public Date getApproved() {
        return approved;
    }

    @Convert(DateNumericStringConverter.class)
    public void setApproved(Date approved) {
        this.approved = approved;
    }

}

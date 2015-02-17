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

package org.neo4j.ogm.domain.education;

import java.util.HashSet;
import java.util.Set;

public class School extends DomainObject {

    private Set<Teacher> teachers = new HashSet<>();
    private String name;

    public School() {}

    public School(String name) {
        setName(name);
    }

    public Set<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(Iterable<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            if (!this.teachers.contains(teacher)) {
                teacher.setSchool(this);
                this.teachers.add(teacher);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

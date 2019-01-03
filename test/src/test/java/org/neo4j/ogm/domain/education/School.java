/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.domain.education;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.PostLoad;

/**
 * @author Vince Bickers
 */
public class School extends DomainObject {

    private Set<Teacher> teachers = new HashSet<>();
    private String name;

    public School() {
    }

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

    @PostLoad
    public void postLoad() {
        for (Teacher teacher : teachers) {
            teacher.setSchool(this);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

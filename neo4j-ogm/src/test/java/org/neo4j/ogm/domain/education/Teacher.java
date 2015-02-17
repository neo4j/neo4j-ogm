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

import java.util.List;

public class Teacher {

    private String name;
    private List<Course> courses;
    private Long id;
    private School school;

    public Teacher() {}

    public Teacher(String name) {
        setName(name);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // @Lazy
    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        // persistable?
        this.courses = courses;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {

        if (this.school != null) {
            this.school.getTeachers().remove(this);
        }

        this.school = school;

        if (this.school != null) {
            this.school.getTeachers().add(this);
        }

    }

}

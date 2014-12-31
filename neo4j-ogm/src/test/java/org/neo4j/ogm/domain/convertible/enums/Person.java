package org.neo4j.ogm.domain.convertible.enums;

import org.neo4j.ogm.annotation.EnumString;

public class Person {

    private Long id;
    private String name;

    @EnumString(Gender.class)
    private Gender gender;

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}

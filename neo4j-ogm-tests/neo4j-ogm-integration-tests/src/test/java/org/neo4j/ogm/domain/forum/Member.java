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
package org.neo4j.ogm.domain.forum;

import java.util.Date;
import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.activity.Activity;

/**
 * @author Vince Bickers
 */
@NodeEntity(label = "User")
public class Member extends Login {

    private IMembership memberShip;
    private Date renewalDate;
    @Relationship(type = "HAS_ACTIVITY")
    private Iterable<Activity> activityList;
    private List<Member> followers;
    private List<Member> followees;
    private Long membershipNumber;
    private int[] nicknames;

    @Property(readOnly = true)
    private String someComputedValue;

    public IMembership getMemberShip() {
        return memberShip;
    }

    public void setMemberShip(IMembership memberShip) {
        this.memberShip = memberShip;
    }

    public Date getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(Date renewalDate) {
        this.renewalDate = renewalDate;
    }

    public Iterable<Activity> getActivityList() {
        return activityList;
    }

    public void setActivityList(Iterable<Activity> activityList) {
        this.activityList = activityList;
    }

    public List<Member> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Member> followers) {
        this.followers = followers;
    }

    public List<Member> getFollowees() {
        return followees;
    }

    public void setFollowees(List<Member> followees) {
        this.followees = followees;
    }

    public long getMembershipNumber() {
        return membershipNumber;
    }

    public void setMembershipNumber(long membershipNumber) {
        this.membershipNumber = membershipNumber;
    }

    public int[] getNicknames() {
        return nicknames;
    }

    public void setNicknames(int[] nicknames) {
        this.nicknames = nicknames;
    }

    public String getSomeComputedValue() {
        return someComputedValue;
    }

    public void setSomeComputedValue(String someComputedValue) {
        this.someComputedValue = someComputedValue;
    }
}

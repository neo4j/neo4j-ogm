package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.domain.forum.activity.Activity;

import java.util.Date;
import java.util.List;

@NodeEntity(label ="User")
public class Member extends Login  {

    private IMembership memberShip;
    private Date renewalDate;
    private List<Activity> activityList;
    private List<Member> followers;
    private List<Member> followees;
    private Long membershipNumber;
    private int[] nicknames;

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

    @Relationship(type ="HAS_ACTIVITY")
    public List<Activity> getActivityList() {
        return activityList;
    }

    @Relationship(type ="HAS_ACTIVITY")
    public void setActivityList(List<Activity> activityList) {
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
}

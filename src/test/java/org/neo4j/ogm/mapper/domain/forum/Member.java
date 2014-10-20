package org.neo4j.ogm.mapper.domain.forum;

import org.neo4j.ogm.annotation.Label;
import org.neo4j.ogm.mapper.domain.forum.activity.Activity;

import java.util.Date;
import java.util.List;

@Label(name="User")
public class Member extends Login  {

    private IMembership memberShip;
    private Date renewalDate;
    private List<Activity> activityList;
    private List<Member> followers;
    private List<Member> followees;

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

    public List<Activity> getActivityList() {
        return activityList;
    }

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
}

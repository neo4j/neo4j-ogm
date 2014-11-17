package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label ="Gold")
public class GoldMembership extends Membership {

    @Override
    public boolean getCanPost() {
        return true;
    }

    @Override
    public boolean getCanComment() {
        return true;
    }

    @Override
    public boolean getCanFollow() {
        return true;
    }

    @Override
    public IMembership[] getUpgrades() {
        return new IMembership[] {};
    }
}

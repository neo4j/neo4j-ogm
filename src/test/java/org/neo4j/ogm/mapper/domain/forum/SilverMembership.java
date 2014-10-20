package org.neo4j.ogm.mapper.domain.forum;

import org.neo4j.ogm.annotation.Label;

@Label(name="Silver")
public class SilverMembership extends Membership {

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
        return false;
    }

    @Override
    public IMembership[] getUpgrades() {
        return new IMembership[] { new GoldMembership() };
    }
}

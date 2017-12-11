package org.neo4j.ogm.request;

/**
 * @author Frantisek Hartman
 */
public class OptimisticLockingConfig {

    private final int expectedResultsCount;
    private String[] types;
    private String versionProperty;

    public OptimisticLockingConfig(int expectedResultsCount, String[] types, String versionProperty) {
        this.expectedResultsCount = expectedResultsCount;
        this.types = types;
        this.versionProperty = versionProperty;
    }

    public int getExpectedResultsCount() {
        return expectedResultsCount;
    }

    public String[] getTypes() {
        return types;
    }

    public String getVersionProperty() {
        return versionProperty;
    }
}

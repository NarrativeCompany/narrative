package org.narrative.network.core.system;

/**
 * Date: 10/26/18
 * Time: 9:43 AM
 *
 * @author brian
 */
public enum EnvironmentType {
    LOCAL,
    DEV,
    STAGING,
    PRODUCTION,
    // uninitialized is the default. unit tests were failing without a value set, so this will be the default
    UNINITIALIZED;

    public boolean isUsesSsl() {
        // bl: use SSL on all environments other than local.
        return !isLocal();
    }

    public boolean isLocal() {
        return this==LOCAL;
    }

    public boolean isDev() {
        return this==DEV;
    }

    public boolean isStaging() {
        return this==STAGING;
    }

    public boolean isProduction() {
        return this==PRODUCTION;
    }

    public boolean isUninitialized() {
        return this==UNINITIALIZED;
    }
}

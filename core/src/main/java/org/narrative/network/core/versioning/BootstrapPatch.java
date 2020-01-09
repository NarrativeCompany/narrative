package org.narrative.network.core.versioning;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Mar 22, 2006
 * Time: 11:12:49 AM
 * <p>
 * Bootstrap patches will be run every time the patch runner is executed.  These patches are guaranteed to be
 * run before any network setup queries have been run, so it may perform any table altering or other bootstrapping
 * issues.
 * <p>
 * IMPORTANT:  Since these patches will be run every time, it is required that these patches be resilliant to this.
 * They should be able to detect themselves if they should run, and they should exit gracefully if they are not
 * required to be run.
 */
public interface BootstrapPatch extends NamedPatch {
    void applyPatch();
}

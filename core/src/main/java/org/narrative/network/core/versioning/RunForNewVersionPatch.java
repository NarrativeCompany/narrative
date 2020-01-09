package org.narrative.network.core.versioning;

/**
 * Marker interface to indicate a patch should always run on new version
 * Only run this patch once per new version on the application.  On dev boxes this will run every time.
 * Date: 10/19/17
 * Time: 3:54 PM
 *
 * @author brian
 */
public interface RunForNewVersionPatch {}

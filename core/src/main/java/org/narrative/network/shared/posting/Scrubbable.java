package org.narrative.network.shared.posting;

/**
 * Scrubbable
 * This interface can be implemented by any
 * class that needs to scrub/clean its content
 * Date: Oct 27, 2004
 *
 * @author Brian Lenz
 */
public interface Scrubbable {
    /**
     * scrub the content in this class,
     * e.g. perform String.trim() on Strings, etc.
     */
    public void scrub();
}

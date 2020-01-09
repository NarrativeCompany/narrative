package org.narrative.common.persistence;

import org.narrative.common.persistence.hibernate.GSession;

import java.util.Collection;

/**
 * Date: Apr 30, 2008
 * Time: 3:01:30 PM
 *
 * @author Steven Benitez
 */
public interface GSessionContainer {
    public Collection<GSession> getSessions();
}

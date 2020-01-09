package org.narrative.network.customizations.narrative.elections.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 11/12/18
 * Time: 10:03 AM
 *
 * @author jonmark
 */
public class ElectionDAO extends GlobalDAOImpl<Election, OID> {
    public ElectionDAO() {
        super(Election.class);
    }
}

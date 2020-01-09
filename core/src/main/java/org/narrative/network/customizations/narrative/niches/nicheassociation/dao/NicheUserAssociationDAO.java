package org.narrative.network.customizations.narrative.niches.nicheassociation.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
public class NicheUserAssociationDAO extends GlobalDAOImpl<NicheUserAssociation, OID> {
    public NicheUserAssociationDAO() {
        super(NicheUserAssociation.class);
    }

}
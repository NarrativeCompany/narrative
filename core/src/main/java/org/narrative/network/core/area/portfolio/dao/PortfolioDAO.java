package org.narrative.network.core.area.portfolio.dao;

import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.shared.daobase.GlobalDAOImpl;

/**
 * Date: 7/25/12
 * Time: 10:45 AM
 * User: jonmark
 */
public class PortfolioDAO extends GlobalDAOImpl<Portfolio, OID> {
    public PortfolioDAO() {
        super(Portfolio.class);
    }

}

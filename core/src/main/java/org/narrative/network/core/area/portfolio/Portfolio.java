package org.narrative.network.core.area.portfolio;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.portfolio.dao.PortfolioDAO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import java.util.List;

/**
 * Date: 7/25/12
 * Time: 10:43 AM
 * User: jonmark
 */
@Entity
@Proxy
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Portfolio implements DAOObject<PortfolioDAO> {
    private OID oid;
    private AreaRlm areaRlm;
    // this is necessary for cascading deletes and its a list for HQL queries
    private List<Niche> niches;

    public static final String FIELD__AREA_RLM__NAME = "areaRlm";

    /**
     * @deprecated for hibernate use only
     */
    public Portfolio() {}

    public Portfolio(AreaRlm areaRlm) {
        this.areaRlm = areaRlm;
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @Transient
    public Area getArea() {
        return AreaRlm.getArea(getAreaRlm());
    }

    @ManyToOne(optional = false)
    @ForeignKey(name = "fk_portfolio_areaRlm")
    public AreaRlm getAreaRlm() {
        return areaRlm;
    }

    public void setAreaRlm(AreaRlm areaRlm) {
        this.areaRlm = areaRlm;
    }

    @OneToMany(mappedBy = Niche.FIELD__PORTFOLIO__NAME, cascade = javax.persistence.CascadeType.ALL)
    public List<Niche> getNiches() {
        return niches;
    }

    public void setNiches(List<Niche> niches) {
        this.niches = niches;
    }

    public static PortfolioDAO dao() {
        return DAOImpl.getDAO(Portfolio.class);
    }
}

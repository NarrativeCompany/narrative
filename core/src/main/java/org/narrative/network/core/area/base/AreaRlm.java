package org.narrative.network.core.area.base;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.network.core.area.base.dao.AreaRlmDAO;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.propertyset.area.AreaPropertySet;
import org.narrative.network.core.propertyset.base.PropertySetType;
import org.narrative.network.core.propertyset.base.services.PropertySetTypeUtil;
import org.narrative.network.core.settings.area.community.advanced.SandboxedCommunitySettings;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.AuthZoneDataType;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.util.List;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:13:52 PM
 */
@Entity
@Proxy
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AreaRlm implements DAOObject<AreaRlmDAO> {

    private OID oid;
    private AuthZone authZone;
    private AreaPropertySet settingsSet;
    private Portfolio defaultPortfolio;
    private List<Portfolio> portfolios;
    private transient SandboxedCommunitySettings sandboxedCommunitySettings;

    public static final String FIELD__SETTINGS_SET__NAME = "settingsSet";

    @Deprecated
    public AreaRlm() {
    }

    /**
     * create an AreaRlm for an area that is being created.
     *
     * @param area the Area off of which this AreaRlm is being based
     */
    public AreaRlm(Area area) {
        this.oid = area.getOid();
        this.authZone = area.getAuthZone();
        this.settingsSet = new AreaPropertySet(PropertySetType.getPropertySetTypeByInterface(SandboxedCommunitySettings.class).getDefaultPropertySet());
        this.portfolios = newLinkedList();
    }

    @Id
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @NotNull
    @Type(type = AuthZoneDataType.TYPE)
    public AuthZone getAuthZone() {
        return authZone;
    }

    public void setAuthZone(AuthZone authZone) {
        this.authZone = authZone;
    }

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = "FK36BA2186282785FC")
    public AreaPropertySet getSettingsSet() {
        return settingsSet;
    }

    public void setSettingsSet(AreaPropertySet settingsSet) {
        this.settingsSet = settingsSet;
    }

    @ManyToOne(optional = true)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = "fk_areaRlm_portfolio")
    public Portfolio getDefaultPortfolio() {
        return defaultPortfolio;
    }

    public void setDefaultPortfolio(Portfolio defaultPortfolio) {
        this.defaultPortfolio = defaultPortfolio;
    }

    @Transient
    public SandboxedCommunitySettings getSandboxedCommunitySettings() {
        if (sandboxedCommunitySettings == null) {
            sandboxedCommunitySettings = PropertySetTypeUtil.getPropertyWrapper(SandboxedCommunitySettings.class, getSettingsSet());
        }
        return sandboxedCommunitySettings;
    }

    /**
     * @return List of all Portfolios associated to this area (including default and deleted)
     * @deprecated Should use getCustomPortfolios unless you want to include the default and deleted in the list which should not ever be desired during regular use
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = Portfolio.FIELD__AREA_RLM__NAME, cascade = javax.persistence.CascadeType.ALL)
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }

    /**
     * @return the area for this AreaRlm
     * @deprecated use static getArea(areaRlm) instead
     */
    @Transient
    public Area getArea() {
        return Area.dao().get(oid);
    }

    @Transient
    public static Area getArea(AreaRlm areaRlm) {
        if (!exists(areaRlm)) {
            return null;
        }
        return Area.dao().get(areaRlm.getOid());
    }

    public static AreaRlmDAO dao() {
        return DAOImpl.getDAO(AreaRlm.class);
    }
}

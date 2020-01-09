package org.narrative.network.core.area.base;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Length;
import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.area.base.dao.AreaDAO;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.fileondisk.image.ImageOnDisk;
import org.narrative.network.core.security.area.base.AreaRole;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.AreaResource;
import org.narrative.network.core.security.area.community.advanced.AreaResourceImpl;
import org.narrative.network.core.security.area.community.advanced.AreaResourceType;
import org.narrative.network.core.system.NetworkRegistry;
import org.narrative.network.core.user.AuthZone;
import org.narrative.network.core.user.NarrativeAuthZoneMaster;
import org.narrative.network.customizations.narrative.services.ReactRoute;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: Nov 23, 2005
 * Time: 3:14:00 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Area implements DAOObject<AreaDAO>, AreaResourceImpl {

    private OID oid;
    private String areaName;
    private Timestamp creationDate;
    protected Timestamp expirationDate;
    private boolean isPublication = false;
    private ImageOnDisk areaPicture;
    private AreaStats areaStats;

    private Collection<AreaUser> areaUsers;

    private Map<OID, AreaResource> areaResources;
    private Set<AreaCircle> areaCircles;

    private AreaResource areaResource;

    private transient AuthZone authZone;

    public Area(String areaName) {
        this.areaName = areaName;
        areaStats = new AreaStats(this);
        creationDate = new Timestamp(System.currentTimeMillis());

        this.areaCircles = newHashSet();
        this.areaResources = newHashMap();
    }

    /**
     * @deprecated Hibernate use only
     */
    public Area() {}

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    @ForeignKey(name = "FK1F44AD9192628A")
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    public static final int MIN_AREA_NAME_LENGTH = 1;
    public static final int MAX_AREA_NAME_LENGTH = 80;

    @NotNull
    @Length(min = MIN_AREA_NAME_LENGTH, max = MAX_AREA_NAME_LENGTH)
    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    @Transient
    public String getAreaNameResolved() {
        return NarrativeAuthZoneMaster.NARRATIVE_NAME;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @JoinColumn(name = Area.FIELD__OID__NAME)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    public AreaStats getAreaStats() {
        return areaStats;
    }

    public void setAreaStats(AreaStats areaStats) {
        this.areaStats = areaStats;
    }

    /**
     * @return nothing
     * @deprecated for hql and cascading only
     */
    @OneToMany(mappedBy = AreaUser.FIELD__AREA__NAME, fetch = FetchType.LAZY)
    @Cascade({CascadeType.REMOVE})
    public Collection<AreaUser> getAreaUsers() {
        return areaUsers;
    }

    public void setAreaUsers(Collection<AreaUser> areaUsers) {
        this.areaUsers = areaUsers;
    }

    @NotNull
    public boolean isPublication() {
        return isPublication;
    }

    public void setPublication(boolean publication) {
        isPublication = publication;
    }

    @Transient
    public static AreaRlm getAreaRlm(Area area) {
        if (!exists(area)) {
            return null;
        }
        return AreaRlm.dao().get(area.getOid());
    }

    @NotNull
    @Index(name = "area_creation_date_idx")
    //@Temporal(TemporalType.TIMESTAMP)
    public Timestamp getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Timestamp creationDate) {
        this.creationDate = creationDate;
    }

    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "FK1F44AD8130E1C6")
    @Cascade({CascadeType.SAVE_UPDATE, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    public ImageOnDisk getAreaPicture() {
        return areaPicture;
    }

    public void setAreaPicture(ImageOnDisk areaPicture) {
        this.areaPicture = areaPicture;
    }

    @Transient
    public boolean isSslEnabled() {
        // bl: the platform will always be SSL except on dev environments
        return NetworkRegistry.getInstance().isNarrativePlatformSsl();
    }

    /**
     * Gets the primary area url for this area
     *
     * @return the primary area url for this area
     */
    @Transient
    public String getPrimaryAreaUrl() {
        String url = getPrimaryAreaDomainName(isSslEnabled() ? "https://" : "http://");
        // bl: front end URLs on local environments should link to port 3000 so it uses the node front end
        if(NetworkRegistry.getInstance().isLocalServer()) {
            url += ":3000";
        }
        return url;
    }

    /**
     * get the domain to use for this area
     *
     * @return the domain of this area
     */
    @Transient
    public String getPrimaryAreaDomainName() {
        return getPrimaryAreaDomainName(null);
    }

    /**
     * get the domain to use for this area
     *
     * @return the domain of this area
     */
    @Transient
    private String getPrimaryAreaDomainName(String prepend) {
        StringBuilder sb = new StringBuilder();

        // bl: every site should use the Narrative platform domain now. ignore the database.
        sb.append(NetworkRegistry.getInstance().getNarrativePlatformDomain());

        if (prepend != null) {
            sb.insert(0, prepend);
        }

        return sb.toString();
    }

    @Transient
    public AreaRole getAreaRoleForCurrentUser() {
        assert isEqual(networkContext().getAuthZone(), getAuthZone()) : "Should only ever attempt to get AreaRole within the same AuthZone!";
        // bl: if this Area is already in scope, then just use the AreaRole directly off of the AreaContext. easy.
        if (areaContext() != null && isEqual(areaContext().getArea(), this)) {
            return areaContext().getAreaRole();
        }
        // if this Area isn't in scope, then get the AreaRole from the PrimaryRole.
        return networkContext().getPrimaryRole().getAreaRoleForArea(this);
    }

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @Cascade({CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.DELETE, CascadeType.SAVE_UPDATE, CascadeType.REPLICATE, CascadeType.DELETE_ORPHAN, CascadeType.LOCK, CascadeType.EVICT})
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    @Override
    public AreaResource getAreaResource() {
        return areaResource;
    }

    public void setAreaResource(AreaResource areaResource) {
        this.areaResource = areaResource;
    }

    @Transient
    public void addDefaultAreaResource() {
        areaResource = new AreaResource(this);
        AreaResource.dao().save(areaResource);
        this.areaResources.put(areaResource.getOid(), areaResource);
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = AreaResource.FIELD__AREA__NAME, cascade = javax.persistence.CascadeType.ALL)
    @MapKey(name = AreaResource.FIELD__OID__NAME)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Map<OID, AreaResource> getAreaResources() {
        return areaResources;
    }

    public void setAreaResources(Map<OID, AreaResource> areaResources) {
        this.areaResources = areaResources;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = AreaCircle.FIELD__AREA__NAME, cascade = javax.persistence.CascadeType.ALL)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    public Set<AreaCircle> getAreaCircles() {
        return areaCircles;
    }

    public void setAreaCircles(Set<AreaCircle> areaCircles) {
        this.areaCircles = areaCircles;
    }

    @Transient
    @Override
    public Area getArea() {
        return this;
    }

    @Transient
    @Override
    public AreaResourceType getAreaResourceType() {
        return AreaResourceType.AREA;
    }

    @Transient
    @Override
    public Portfolio getPortfolio() {
        // bl: the portfolio for the AreaResourceImpl is just the default portfolio
        return getAreaRlm(this).getDefaultPortfolio();
    }

    @Transient
    @Override
    public String getNameForDisplay() {
        return getAreaNameResolved();
    }

    @Transient
    public AuthZone getAuthZone() {
        if (authZone == null) {
            authZone = AuthZone.getAuthZone(this);
        }
        return authZone;
    }

    @Transient
    public String getManageNotificationsUrl() {
        return ReactRoute.MEMBER_NOTIFICATION_SETTINGS.getUrl();
    }

    public static AreaDAO dao() {
        return DAOImpl.getDAO(Area.class);
    }
}

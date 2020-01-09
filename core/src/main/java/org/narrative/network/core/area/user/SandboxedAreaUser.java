package org.narrative.network.core.area.user;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.user.dao.SandboxedAreaUserDAO;
import org.narrative.network.core.user.PasswordFields;
import org.narrative.network.core.user.User;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.validation.constraints.NotNull;

/**
 * Date: Dec 18, 2009
 * Time: 12:42:10 PM
 *
 * @author Jonmark Weber
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SandboxedAreaUser implements DAOObject<SandboxedAreaUserDAO> {
    private OID oid;

    private AreaUserRlm areaUserRlm;
    private AreaRlm areaRlm;

    private String displayName;

    private boolean disableNewDialogs;

    public static final String FIELD__AREA_USER_RLM__NAME = "areaUserRlm";
    public static final String FIELD__AREA_RLM__NAME = "areaRlm";
    public static final String FIELD__DISPLAY_NAME__NAME = "displayName";

    /**
     * @deprecated for hibernate use only
     */
    public SandboxedAreaUser() {}

    public SandboxedAreaUser(AreaUserRlm areaUserRlm, String displayName) {
        this.areaUserRlm = areaUserRlm;
        this.areaRlm = areaUserRlm.getAreaRlm();
        this.displayName = displayName;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__AREA_USER_RLM__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    @PrimaryKeyJoinColumn
    public AreaUserRlm getAreaUserRlm() {
        return areaUserRlm;
    }

    public void setAreaUserRlm(AreaUserRlm areaUserRlm) {
        this.areaUserRlm = areaUserRlm;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = "fk_sandboxedAreaUser_areaRlm")
    public AreaRlm getAreaRlm() {
        return areaRlm;
    }

    public void setAreaRlm(AreaRlm areaRlm) {
        this.areaRlm = areaRlm;
    }

    @NotNull
    @Length(min = User.MIN_DISPLAY_NAME_LENGTH, max = User.MAX_DISPLAY_NAME_LENGTH)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isDisableNewDialogs() {
        return disableNewDialogs;
    }

    public void setDisableNewDialogs(boolean disableNewDialogs) {
        this.disableNewDialogs = disableNewDialogs;
    }

    public static SandboxedAreaUserDAO dao() {
        return NetworkDAOImpl.getDAO(SandboxedAreaUser.class);
    }
}

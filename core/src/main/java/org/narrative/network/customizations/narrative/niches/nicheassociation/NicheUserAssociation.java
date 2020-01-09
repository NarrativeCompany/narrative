package org.narrative.network.customizations.narrative.niches.nicheassociation;

import org.narrative.common.persistence.DAOImpl;
import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.nicheassociation.dao.NicheUserAssociationDAO;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.sql.Timestamp;
import java.util.Comparator;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: martin
 * Date: 12/2/2018
 * Time: 09:20
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {NicheUserAssociation.FIELD__NICHE__COLUMN, NicheUserAssociation.FIELD__AREA_USER_RLM__COLUMN}), @UniqueConstraint(columnNames = {NicheUserAssociation.FIELD__AREA_USER_RLM__COLUMN, NicheUserAssociation.FIELD__ASSOCIATION_SLOT__COLUMN})})
public class NicheUserAssociation implements DAOObject<NicheUserAssociationDAO> {
    public static final Comparator<NicheUserAssociation> NICHE_COMPARATOR = (o1, o2) -> Niche.NAME_COMPARATOR.compare(o1.getNiche(), o2.getNiche());

    private OID oid;
    private Niche niche;
    private AreaUserRlm areaUserRlm;
    private AssociationType type;
    private NicheAssociationSlot associationSlot;
    private Timestamp associationDatetime;

    public static final String FIELD__NICHE__NAME = "niche";
    public static final String FIELD__AREA_USER_RLM__NAME = "areaUserRlm";
    public static final String FIELD__ASSOCIATION_SLOT__NAME = "associationSlot";

    public static final String FIELD__NICHE__COLUMN = FIELD__NICHE__NAME + "_" + Niche.FIELD__OID__NAME;
    public static final String FIELD__AREA_USER_RLM__COLUMN = FIELD__AREA_USER_RLM__NAME + "_" + AreaUserRlm.FIELD__OID__NAME;
    public static final String FIELD__ASSOCIATION_SLOT__COLUMN = FIELD__ASSOCIATION_SLOT__NAME;

    @Deprecated
    public NicheUserAssociation() { }

    public NicheUserAssociation(Niche niche, AreaUserRlm areaUserRlm, AssociationType type) {
        assert !areaUserRlm.getAvailableNicheAssociationSlots().isEmpty() : "Should always have a free association slot when using the constructor!";

        this.niche = niche;
        this.areaUserRlm = areaUserRlm;
        this.type = type;
        // jw: just use the first slot on the user.
        this.associationSlot = areaUserRlm.getAvailableNicheAssociationSlots().iterator().next();
        this.associationDatetime = now();

        // jw: I considered adding this to the users nicheAssociations, but that seems like overkill currently.
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheUserAssociation_niche")
    public Niche getNiche() {
        return niche;
    }

    public void setNiche(Niche niche) {
        this.niche = niche;
    }

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheUserAssociation_areaUserRlm")
    public AreaUserRlm getAreaUserRlm() {
        return areaUserRlm;
    }

    public void setAreaUserRlm(AreaUserRlm areaUserRlm) {
        this.areaUserRlm = areaUserRlm;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public AssociationType getType() {
        return type;
    }

    public void setType(AssociationType type) {
        this.type = type;
    }

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    public NicheAssociationSlot getAssociationSlot() {
        return associationSlot;
    }

    public void setAssociationSlot(NicheAssociationSlot slot) {
        this.associationSlot = slot;
    }

    @NotNull
    public Timestamp getAssociationDatetime() {
        return associationDatetime;
    }

    public void setAssociationDatetime(Timestamp associationTimestamp) {
        this.associationDatetime = associationTimestamp;
    }

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    public OID getOid() {
        return oid;
    }

    @Override
    public void setOid(OID oid) {
        this.oid = oid;
    }

    public static NicheUserAssociationDAO dao() {
        return DAOImpl.getDAO(NicheUserAssociation.class);
    }
}
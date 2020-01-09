package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jetbrains.annotations.NotNull;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Date: 10/8/11
 * Time: 8:55 PM
 *
 * @author brian
 */
@MappedSuperclass
public abstract class CompositionDetail {
    private OID oid;
    private Composition composition;

    private static final String FIELD__COMPOSITION__NAME = "composition";

    /**
     * @deprecated for hibernate use only
     */
    protected CompositionDetail() {}

    public CompositionDetail(Composition composition) {
        this.composition = composition;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__COMPOSITION__NAME)})
    public OID getOid() {
        return oid;
    }

    public void setOid(OID oid) {
        this.oid = oid;
    }

    // bl: have to make this transient in order to allow the foreign key name for this field to be different
    // on each of the concrete sub-types (in order to maintain backward compatibility of foreign key names
    // following the big genesis code refactor).
    @Transient
    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    @Transient
    @NotNull
    public abstract NetworkDAOImpl getDAO();
}

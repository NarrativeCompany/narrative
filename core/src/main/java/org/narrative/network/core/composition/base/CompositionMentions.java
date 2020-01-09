package org.narrative.network.core.composition.base;

import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.core.composition.base.dao.CompositionMentionsDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Transient;

/**
 * Created by IntelliJ IDEA.
 * User: jonmark
 * Date: 7/26/16
 * Time: 1:30 PM
 */
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CompositionMentions extends PostMentionsBase<CompositionMentionsDAO> {
    private OID oid;
    private Composition composition;

    public static final String FIELD__COMPOSITION__NAME = "composition";

    /**
     * @deprecated For Hibernate use only!
     */
    public CompositionMentions() {}

    public CompositionMentions(Composition composition) {
        this.composition = composition;
    }

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__COMPOSITION__NAME)})
    public OID getOid() {
        return oid;
    }

    @Override
    public void setOid(OID oid) {
        this.oid = oid;
    }

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    @PrimaryKeyJoinColumn
    public Composition getComposition() {
        return composition;
    }

    public void setComposition(Composition composition) {
        this.composition = composition;
    }

    @Override
    @Transient
    protected PostBase getPostBase() {
        return getComposition();
    }

    public static CompositionMentionsDAO dao() {
        return NetworkDAOImpl.getDAO(CompositionMentions.class);
    }
}

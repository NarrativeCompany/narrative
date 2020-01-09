package org.narrative.network.customizations.narrative.niches.elections;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.niches.elections.dao.NicheModeratorElectionDAO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/12/18
 * Time: 10:15 AM
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class NicheModeratorElection implements DAOObject<NicheModeratorElectionDAO> {
    public static final String FIELD__ELECTION__NAME = "election";

    public static final String FIELD__ELECTION__COLUMN = FIELD__ELECTION__NAME + "_" + Election.FIELD__OID__NAME;

    @Id
    @GeneratedValue(generator = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME)
    @GenericGenerator(name = HibernateUtil.FOREIGN_GENERIC_GENERATOR_NAME, strategy = HibernateUtil.FOREIGN_STRATEGY, parameters = {@Parameter(name = HibernateUtil.FOREIGN_STRATEGY_PROPERTY_NAME, value = FIELD__ELECTION__NAME)})
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheModeratorElection_niche")
    private Niche niche;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_nicheModeratorElection_election")
    @PrimaryKeyJoinColumn
    private Election election;

    /**
     * @deprecated for hibernate use only
     */
    public NicheModeratorElection() {}

    public NicheModeratorElection(Niche niche, Election election) {
        assert exists(niche) : "Should always be given a niche!";
        assert niche.getStatus().isActive() : "Specified niche should always be approved by the time we get here!";
        assert !exists(niche.getActiveModeratorElection()) : "Specified niche should not already have an active moderator election!";
        assert exists(election) : "Should always be provided an election!";
        assert election.getAvailableSlots() <= niche.getOpenModeratorSlots() : "Should never attempt to create an election for more slots than the Niche supports.";

        this.niche = niche;
        this.election = election;
    }

    public static NicheModeratorElectionDAO dao() {
        return NetworkDAOImpl.getDAO(NicheModeratorElection.class);
    }
}

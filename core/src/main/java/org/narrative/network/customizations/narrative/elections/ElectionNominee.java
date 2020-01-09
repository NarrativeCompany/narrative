package org.narrative.network.customizations.narrative.elections;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.elections.dao.ElectionNomineeDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.Setter;
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

import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 11/12/18
 * Time: 10:33 AM
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Table(uniqueConstraints = {@UniqueConstraint(
        name = "uk_electionNominee_nominee_election",
        columnNames = {ElectionNominee.FIELD__NOMINEE__COLUMN, ElectionNominee.FIELD__ELECTION__COLUMN}
)})
public class ElectionNominee implements DAOObject<ElectionNomineeDAO> {
    public static final String FIELD__ELECTION__NAME = "election";
    public static final String FIELD__NOMINEE__NAME = "nominee";

    public static final String FIELD__NOMINEE__COLUMN = FIELD__NOMINEE__NAME + "_" + User.FIELD__OID__COLUMN;
    public static final String FIELD__ELECTION__COLUMN = FIELD__ELECTION__NAME + "_" + Election.FIELD__OID__COLUMN;

    public static final int MAX_PERSONAL_STATEMENT_SIZE = 140;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_electionNominee_election")
    private Election election;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @ForeignKey(name = "fk_electionNominee_nominee")
    private User nominee;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private NomineeStatus status;

    @Type(type = HibernateInstantType.TYPE)
    private Instant nominationConfirmedDatetime;

    private String personalStatement;

    /**
     * @deprecated for hibernate use only
     */
    public ElectionNominee() {}

    public ElectionNominee(Election election, User nominee, NomineeStatus status) {
        assert exists(election) : "Should always be provided with a election.";
        assert exists(nominee) : "Should always be provided with a nominee.";
        assert election.getType().isCanUserBeNominated(nominee) : "Should never specify a user that cannot be nominated!";
        assert status != null : "Should always have a status specified!";

        this.election = election;
        this.nominee = nominee;
        this.status = status;

        this.nominationConfirmedDatetime = status.isConfirmed() ? Instant.now() : null;
    }

    public static ElectionNomineeDAO dao() {
        return NetworkDAOImpl.getDAO(ElectionNominee.class);
    }
}

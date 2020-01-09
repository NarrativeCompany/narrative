package org.narrative.network.customizations.narrative.elections;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernateUtil;
import org.narrative.common.persistence.hibernate.IntegerEnumType;
import org.narrative.network.core.cluster.partition.PartitionGroup;
import org.narrative.network.customizations.narrative.elections.dao.ElectionDAO;
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
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.narrative.common.util.CoreUtils.*;
import static org.narrative.network.shared.util.NetworkCoreUtils.*;

/**
 * Date: 11/12/18
 * Time: 10:01 AM
 *
 * @author jonmark
 */
@Getter
@Setter
@Entity
@Proxy
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Election implements DAOObject<ElectionDAO> {

    private static final String HAS_JIT_INITED_NOMINEE_COUNT_PROPERTY = Election.class.getName() + "-HasJITInitedNomineeCount";
    private static final String HAS_JIT_INITED_CURRENT_USER_NOMINEE_PROPERTY = Election.class.getName() + "-HasJITInitedCurrentUserNominee";

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private ElectionType type;

    @NotNull
    @Type(type = IntegerEnumType.TYPE)
    private ElectionStatus status;

    private int availableSlots;

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    private Instant nominationStartDatetime;

    // jw: not adding any ordering here since we do not expect to actually use this column. This is purely here for
    //     joining purposes.
    @OneToMany(fetch = FetchType.LAZY, mappedBy = ElectionNominee.FIELD__ELECTION__NAME)
    @ForeignKey(name = HibernateUtil.NO_FOREIGN_KEY_NAME)
    private Set<ElectionNominee> nominees;

    /**
     * @deprecated for hibernate use only
     */
    public Election() {}

    public Election(ElectionType type, int availableSlots) {
        assert type != null : "Should always be provided a election type during construction!";
        assert availableSlots > 0 : "What is the point of creating an election with no available slots?";

        this.type = type;
        this.status = ElectionStatus.NOMINATING;
        this.availableSlots = availableSlots;
        this.nominationStartDatetime = Instant.now();
    }

    private transient Integer nomineeCount;

    @Transient
    public Integer getNomineeCount() {
        if (nomineeCount == null) {
            // jw: let's ensure that we have not already initialized this value before
            PartitionGroup.getCurrentPartitionGroup().performSingleInstanceJitSafetyChecks(HAS_JIT_INITED_NOMINEE_COUNT_PROPERTY);

            // jw: if we got here, then we are clear to JIT this bad boy!
            ElectionNominee.dao().populateElectionNomineeCounts(Collections.singleton(this));

            assert nomineeCount != null : "Should always have a nominee count set now!";
        }

        return nomineeCount;
    }

    private transient Boolean hasSetCurrentUserNominee;
    private transient ElectionNominee currentUserNominee;

    @Transient
    public ElectionNominee getCurrentUserNominee() {
        if (hasSetCurrentUserNominee == null) {
            // jw: let's ensure that we have not already initialized this value before
            PartitionGroup.getCurrentPartitionGroup().performSingleInstanceJitSafetyChecks(HAS_JIT_INITED_CURRENT_USER_NOMINEE_PROPERTY);

            ElectionNominee currentUserNominee = null;
            // jw: if we got here, then we are clear to JIT this bad boy!
            if (networkContext().isLoggedInUser()) {
                currentUserNominee = ElectionNominee.dao().getForUser(this, networkContext().getUser());
            }
            setCurrentUserNominee(currentUserNominee);
        }

        return currentUserNominee;
    }

    public void setCurrentUserNominee(ElectionNominee currentUserNominee) {
        // bl: exclude the currentUserNominee if it's rejected/withdrawn since it's really not necessary for the UI
        if(exists(currentUserNominee) && currentUserNominee.getStatus().isNegativeType()) {
            currentUserNominee = null;
        }
        this.currentUserNominee = currentUserNominee;
        this.hasSetCurrentUserNominee = Boolean.TRUE;
    }

    public static ElectionDAO dao() {
        return NetworkDAOImpl.getDAO(Election.class);
    }
}

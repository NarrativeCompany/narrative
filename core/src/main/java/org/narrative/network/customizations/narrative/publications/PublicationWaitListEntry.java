package org.narrative.network.customizations.narrative.publications;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.util.NarrativeConstants;
import org.narrative.network.core.narrative.rewards.services.RewardUtils;
import org.narrative.network.core.user.User;
import org.narrative.network.customizations.narrative.publications.dao.PublicationWaitListEntryDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;

/**
 * Date: 9/23/19
 * Time: 12:44 PM
 *
 * @author brian
 */
@Data
@Entity
@NoArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(name="publicationWaitListEntry_emailAddress_uidx", columnNames = {PublicationWaitListEntry.FIELD__EMAIL_ADDRESS__COLUMN}),
        @UniqueConstraint(name="publicationWaitListEntry_publication_uidx", columnNames = {PublicationWaitListEntry.FIELD__PUBLICATION__COLUMN})
})
@FieldNameConstants
public class PublicationWaitListEntry implements DAOObject<PublicationWaitListEntryDAO> {
    public static final String FIELD__EMAIL_ADDRESS__NAME = "emailAddress";
    public static final String FIELD__EMAIL_ADDRESS__COLUMN = FIELD__EMAIL_ADDRESS__NAME;
    public static final String FIELD__PUBLICATION__NAME = "publication";
    public static final String FIELD__PUBLICATION__COLUMN = FIELD__PUBLICATION__NAME + "_" + Publication.FIELD__OID__NAME;
    /**
     * the last day that discounts are available is November 30, 2019
     */
    public static final LocalDate DISCOUNT_END_DATE = LocalDate.of(2019, Month.NOVEMBER, 30);
    /**
     * the end instant is simply the end of the end date, which you can get by adding a day from the start of the day.
     * note that this is an _exclusive_ date, so all valid discount times must come BEFORE this instant
     */
    public static final Instant DISCOUNT_END_INSTANT = DISCOUNT_END_DATE.atStartOfDay().plusDays(1).toInstant(RewardUtils.REWARDS_ZONE_OFFSET);

    /**
     * discounts for the wait list are 20%
     */
    public static final BigDecimal DISCOUNT_PERCENTAGE = new BigDecimal("0.2");

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @Length(min = NarrativeConstants.MIN_EMAIL_ADDRESS_LENGTH, max = NarrativeConstants.MAX_EMAIL_ADDRESS_LENGTH)
    @Column(nullable = false)
    private String emailAddress;

    private boolean used;

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_publicationWaitListEntry_claimer")
    private User claimer;

    @ManyToOne(fetch = FetchType.EAGER)
    @ForeignKey(name = "fk_publicationWaitListEntry_publication")
    private Publication publication;

    public PublicationWaitListEntry(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public static boolean isAreWaitListDiscountsAllowedForNewPublications() {
        return Instant.now().isBefore(PublicationWaitListEntry.DISCOUNT_END_INSTANT);
    }

    public static PublicationWaitListEntryDAO dao() {
        return NetworkDAOImpl.getDAO(PublicationWaitListEntry.class);
    }
}

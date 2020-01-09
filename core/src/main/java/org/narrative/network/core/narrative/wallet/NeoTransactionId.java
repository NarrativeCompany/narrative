package org.narrative.network.core.narrative.wallet;

import org.narrative.common.persistence.DAOObject;
import org.narrative.common.persistence.OID;
import org.narrative.common.persistence.OIDGenerator;
import org.narrative.common.persistence.hibernate.HibernateInstantType;
import org.narrative.common.persistence.hibernate.HibernateNrveValueType;
import org.narrative.network.core.narrative.wallet.dao.NeoTransactionIdDAO;
import org.narrative.network.customizations.narrative.NrveValue;
import org.narrative.network.customizations.narrative.neo.services.NeoUtils;
import org.narrative.network.customizations.narrative.neo.services.NeoscanTransactionMetadata;
import org.narrative.network.shared.daobase.NetworkDAOImpl;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import java.time.Instant;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2019-06-14
 * Time: 11:17
 *
 * @author brian
 */
@Getter
@Setter
@Entity
@Proxy
@FieldNameConstants
@NoArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(name = "uidx_neoTransactionId_transactionId", columnNames = {NeoTransactionId.FIELD_TRANSACTION_ID_COLUMN})})
public class NeoTransactionId implements DAOObject<NeoTransactionIdDAO> {
    private static final String FIELD_TRANSACTION_ID_NAME = "transactionId";
    static final String FIELD_TRANSACTION_ID_COLUMN = FIELD_TRANSACTION_ID_NAME;

    @Id
    @GeneratedValue(generator = OIDGenerator.NAME)
    private OID oid;

    @ManyToOne(optional=false)
    @ForeignKey(name = "fk_neoTransactionId_neoTransaction")
    private NeoTransaction neoTransaction;

    @NotNull
    @Type(type = HibernateNrveValueType.TYPE)
    private NrveValue nrveAmount;

    @NotNull
    @Length(min=NeoUtils.NEO_TRANSACTION_ID_LENGTH,max=NeoUtils.NEO_TRANSACTION_ID_LENGTH)
    private String transactionId;

    @NotNull
    @Type(type = HibernateInstantType.TYPE)
    private Instant transactionDatetime;

    private long blockNumber;

    public NeoTransactionId(@NotNull NeoTransaction neoTransaction, NeoscanTransactionMetadata metadata) {
        assert exists(neoTransaction) : "Should always get a NeoTransaction!";
        assert NeoUtils.isValidNeoTransactionId(metadata.getTransactionId()) : "Should always have a valid transaction ID!";

        this.neoTransaction = neoTransaction;
        this.nrveAmount = new NrveValue(metadata.getAmount());
        this.transactionId = metadata.getTransactionId();
        this.transactionDatetime = metadata.getTransactionDatetime();
        this.blockNumber = metadata.getBlockNumber();
    }

    public static NeoTransactionIdDAO dao() {
        return NetworkDAOImpl.getDAO(NeoTransactionId.class);
    }
}

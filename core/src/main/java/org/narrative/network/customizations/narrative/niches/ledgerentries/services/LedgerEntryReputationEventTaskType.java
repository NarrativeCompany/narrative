package org.narrative.network.customizations.narrative.niches.ledgerentries.services;

import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntryType;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation.CreateAupViolationReputationEventTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation.CreateIssueResultReputationEventsTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation.CreateKycReputationApprovedEventTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation.CreateKycReputationRevokedEventTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation.CreateNichePurchaseFailureReputationEventsTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation.CreateNicheReferendumResultReputationEventsTask;
import org.narrative.network.customizations.narrative.niches.ledgerentries.services.reputation.CreatePaymentChargebackReputationEventTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: 2018-12-12
 * Time: 15:37
 *
 * @author jonmark
 */
public enum LedgerEntryReputationEventTaskType {
    NICHE_INVOICE_FAILED(LedgerEntryType.NICHE_INVOICE_FAILED) {
        @Override
        protected CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry) {
            return new CreateNichePurchaseFailureReputationEventsTask(entry);
        }
    },
    ISSUE_REFERENDUM_RESULT(LedgerEntryType.ISSUE_REFERENDUM_RESULT) {
        @Override
        protected CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry) {
            return new CreateIssueResultReputationEventsTask(entry);
        }
    },
    NICHE_REFERENDUM_RESULT(LedgerEntryType.NICHE_REFERENDUM_RESULT) {
        @Override
        protected CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry) {
            return new CreateNicheReferendumResultReputationEventsTask(entry);
        }
    },
    KYC_CERTIFICATION_APPROVED(LedgerEntryType.KYC_CERTIFICATION_APPROVED) {
        @Override
        protected CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry) {
            return new CreateKycReputationApprovedEventTask(entry);
        }
    },
    KYC_CERTIFICATION_REVOKED(LedgerEntryType.KYC_CERTIFICATION_REVOKED) {
        @Override
        protected CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry) {
            return new CreateKycReputationRevokedEventTask(entry);
        }
    },
    PAYMENT_CHARGEBACK(LedgerEntryType.PAYMENT_CHARGEBACK) {
        @Override
        protected CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry) {
            return new CreatePaymentChargebackReputationEventTask(entry);
        }
    },
    AUP_VIOLATION(LedgerEntryType.USER_HAD_POST_OR_COMMENT_DELETED_FOR_AUP_VIOLATION) {
        @Override
        protected CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry) {
            return new CreateAupViolationReputationEventTask(entry);
        }
    },
    ;

    public static final Map<LedgerEntryType, LedgerEntryReputationEventTaskType> BY_LEDGER_ENTRY_TYPE;

    static {
        BY_LEDGER_ENTRY_TYPE = Collections.unmodifiableMap(
                Arrays.stream(LedgerEntryReputationEventTaskType.values()).collect(Collectors.toMap(LedgerEntryReputationEventTaskType::getForLedgerEntryType, Function.identity()))
        );

        assert BY_LEDGER_ENTRY_TYPE.size() == values().length : "Should always have the same number of records in BY_LEDGER_ENTRY_TYPES as we have values!  A ledgerEntryType must be repeated!";
    }

    private final LedgerEntryType forLedgerEntryType;

    LedgerEntryReputationEventTaskType(LedgerEntryType forLedgerEntryType) {
        assert forLedgerEntryType != null : "Should always have a forLedgerEntryType!";
        this.forLedgerEntryType = forLedgerEntryType;
    }

    public LedgerEntryType getForLedgerEntryType() {
        return forLedgerEntryType;
    }

    protected abstract CreateReputationEventsFromLedgerEntryTask getTask(LedgerEntry entry);

    public static CreateReputationEventsFromLedgerEntryTask getCreateReputationEventTask(LedgerEntry entry) {
        assert exists(entry) : "Should always hvae a entry at this point!";

        LedgerEntryReputationEventTaskType taskType = LedgerEntryReputationEventTaskType.BY_LEDGER_ENTRY_TYPE.get(entry.getType());

        // jw: if we do not have a task type for this entry type, then short out.
        if (taskType == null) {
            return null;
        }

        // jw: otherwise give it a chance to create the task.
        return taskType.getTask(entry);
    }
}

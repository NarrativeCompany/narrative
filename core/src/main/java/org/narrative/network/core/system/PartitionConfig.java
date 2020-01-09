package org.narrative.network.core.system;

import org.narrative.network.core.area.base.Area;
import org.narrative.network.core.area.base.AreaRlm;
import org.narrative.network.core.area.base.AreaStats;
import org.narrative.network.core.area.base.ItemHourTrendingStats;
import org.narrative.network.core.area.base.RoleContentPageView;
import org.narrative.network.core.area.base.dao.AreaDAO;
import org.narrative.network.core.area.base.dao.AreaRlmDAO;
import org.narrative.network.core.area.base.dao.AreaStatsDAO;
import org.narrative.network.core.area.base.dao.ItemHourTrendingStatsDAO;
import org.narrative.network.core.area.base.dao.RoleContentPageViewDAO;
import org.narrative.network.core.area.portfolio.Portfolio;
import org.narrative.network.core.area.portfolio.dao.PortfolioDAO;
import org.narrative.network.core.area.user.AreaCredentials;
import org.narrative.network.core.area.user.AreaUser;
import org.narrative.network.core.area.user.AreaUserRlm;
import org.narrative.network.core.area.user.AreaUserStats;
import org.narrative.network.core.area.user.SandboxedAreaUser;
import org.narrative.network.core.area.user.WatchedUser;
import org.narrative.network.core.area.user.dao.AreaCredentialsDAO;
import org.narrative.network.core.area.user.dao.AreaUserDAO;
import org.narrative.network.core.area.user.dao.AreaUserRlmDAO;
import org.narrative.network.core.area.user.dao.AreaUserStatsDAO;
import org.narrative.network.core.area.user.dao.SandboxedAreaUserDAO;
import org.narrative.network.core.area.user.dao.WatchedUserDAO;
import org.narrative.network.core.cluster.partition.Partition;
import org.narrative.network.core.cluster.partition.PartitionType;
import org.narrative.network.core.cluster.partition.dao.PartitionDAO;
import org.narrative.network.core.composition.base.Composition;
import org.narrative.network.core.composition.base.CompositionMentions;
import org.narrative.network.core.composition.base.CompositionStats;
import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.core.composition.base.ReplyMentions;
import org.narrative.network.core.composition.base.ReplyStats;
import org.narrative.network.core.composition.base.dao.CompositionDAO;
import org.narrative.network.core.composition.base.dao.CompositionMentionsDAO;
import org.narrative.network.core.composition.base.dao.CompositionStatsDAO;
import org.narrative.network.core.composition.base.dao.ReplyDAO;
import org.narrative.network.core.composition.base.dao.ReplyMentionsDAO;
import org.narrative.network.core.composition.base.dao.ReplyStatsDAO;
import org.narrative.network.core.composition.files.FilePointer;
import org.narrative.network.core.composition.files.FilePointerSet;
import org.narrative.network.core.composition.files.dao.FilePointerDAO;
import org.narrative.network.core.composition.files.dao.FilePointerSetDAO;
import org.narrative.network.core.content.base.Content;
import org.narrative.network.core.content.base.ContentStats;
import org.narrative.network.core.content.base.FutureContent;
import org.narrative.network.core.content.base.TrendingContent;
import org.narrative.network.core.content.base.dao.ContentDAO;
import org.narrative.network.core.content.base.dao.ContentStatsDAO;
import org.narrative.network.core.content.base.dao.FutureContentDAO;
import org.narrative.network.core.content.base.dao.TrendingContentDAO;
import org.narrative.network.core.fileondisk.base.FileOnDisk;
import org.narrative.network.core.fileondisk.base.FileOnDiskStats;
import org.narrative.network.core.fileondisk.base.dao.FileOnDiskDAO;
import org.narrative.network.core.fileondisk.base.dao.FileOnDiskStatsDAO;
import org.narrative.network.core.moderation.ModeratedContent;
import org.narrative.network.core.moderation.dao.ModeratedContentDAO;
import org.narrative.network.core.narrative.rewards.ContentReward;
import org.narrative.network.core.narrative.rewards.NarrativeCompanyReward;
import org.narrative.network.core.narrative.rewards.NicheContentReward;
import org.narrative.network.core.narrative.rewards.NicheModeratorReward;
import org.narrative.network.core.narrative.rewards.NicheOwnerReward;
import org.narrative.network.core.narrative.rewards.NicheReward;
import org.narrative.network.core.narrative.rewards.ProratedMonthRevenue;
import org.narrative.network.core.narrative.rewards.PublicationReward;
import org.narrative.network.core.narrative.rewards.RewardPeriod;
import org.narrative.network.core.narrative.rewards.RoleContentReward;
import org.narrative.network.core.narrative.rewards.UserActivityReward;
import org.narrative.network.core.narrative.rewards.UserActivityRewardEvent;
import org.narrative.network.core.narrative.rewards.UserElectorateReward;
import org.narrative.network.core.narrative.rewards.UserTribunalReward;
import org.narrative.network.core.narrative.rewards.dao.ContentRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.NarrativeCompanyRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.NicheContentRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.NicheModeratorRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.NicheOwnerRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.NicheRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.ProratedMonthRevenueDAO;
import org.narrative.network.core.narrative.rewards.dao.PublicationRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.RewardPeriodDAO;
import org.narrative.network.core.narrative.rewards.dao.RoleContentRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.UserActivityRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.UserActivityRewardEventDAO;
import org.narrative.network.core.narrative.rewards.dao.UserElectorateRewardDAO;
import org.narrative.network.core.narrative.rewards.dao.UserTribunalRewardDAO;
import org.narrative.network.core.narrative.wallet.NeoTransaction;
import org.narrative.network.core.narrative.wallet.NeoTransactionId;
import org.narrative.network.core.narrative.wallet.NeoWallet;
import org.narrative.network.core.narrative.wallet.Wallet;
import org.narrative.network.core.narrative.wallet.WalletTransaction;
import org.narrative.network.core.narrative.wallet.dao.NeoTransactionDAO;
import org.narrative.network.core.narrative.wallet.dao.NeoTransactionIdDAO;
import org.narrative.network.core.narrative.wallet.dao.NeoWalletDAO;
import org.narrative.network.core.narrative.wallet.dao.WalletDAO;
import org.narrative.network.core.narrative.wallet.dao.WalletTransactionDAO;
import org.narrative.network.core.propertyset.area.AreaPropertyOverride;
import org.narrative.network.core.propertyset.area.AreaPropertySet;
import org.narrative.network.core.propertyset.area.dao.AreaPropertyOverrideDAO;
import org.narrative.network.core.propertyset.area.dao.AreaPropertySetDAO;
import org.narrative.network.core.propertyset.base.Property;
import org.narrative.network.core.propertyset.base.PropertySet;
import org.narrative.network.core.propertyset.base.dao.PropertyDAO;
import org.narrative.network.core.propertyset.base.dao.PropertySetDAO;
import org.narrative.network.core.rating.dao.UserAgeRatedCompositionDAO;
import org.narrative.network.core.rating.dao.UserQualityRatedCompositionDAO;
import org.narrative.network.core.rating.dao.UserQualityRatedReplyDAO;
import org.narrative.network.core.rating.model.UserAgeRatedComposition;
import org.narrative.network.core.rating.model.UserQualityRatedComposition;
import org.narrative.network.core.rating.model.UserQualityRatedReply;
import org.narrative.network.core.security.area.community.advanced.AreaCircle;
import org.narrative.network.core.security.area.community.advanced.AreaCirclePermission;
import org.narrative.network.core.security.area.community.advanced.AreaCircleUser;
import org.narrative.network.core.security.area.community.advanced.AreaResource;
import org.narrative.network.core.security.area.community.advanced.dao.AreaCircleDAO;
import org.narrative.network.core.security.area.community.advanced.dao.AreaCirclePermissionDAO;
import org.narrative.network.core.security.area.community.advanced.dao.AreaCircleUserDAO;
import org.narrative.network.core.security.area.community.advanced.dao.AreaResourceDAO;
import org.narrative.network.core.user.EmailAddress;
import org.narrative.network.core.user.User;
import org.narrative.network.core.user.UserAuth;
import org.narrative.network.core.user.UserKyc;
import org.narrative.network.core.user.UserKycEvent;
import org.narrative.network.core.user.UserStats;
import org.narrative.network.core.user.dao.EmailAddressDAO;
import org.narrative.network.core.user.dao.UserAuthDAO;
import org.narrative.network.core.user.dao.UserDAO;
import org.narrative.network.core.user.dao.UserKycDAO;
import org.narrative.network.core.user.dao.UserKycEventDAO;
import org.narrative.network.core.user.dao.UserStatsDAO;
import org.narrative.network.core.versioning.AppVersion;
import org.narrative.network.core.versioning.AppliedPatch;
import org.narrative.network.core.versioning.PatchRunnerLock;
import org.narrative.network.core.versioning.dao.AppVersionDAO;
import org.narrative.network.core.versioning.dao.AppliedPatchDAO;
import org.narrative.network.core.versioning.dao.PatchRunnerLockDAO;
import org.narrative.network.customizations.narrative.channels.Channel;
import org.narrative.network.customizations.narrative.channels.ChannelDomain;
import org.narrative.network.customizations.narrative.channels.ChannelUser;
import org.narrative.network.customizations.narrative.channels.DeletedChannel;
import org.narrative.network.customizations.narrative.channels.FollowedChannel;
import org.narrative.network.customizations.narrative.channels.dao.ChannelDAO;
import org.narrative.network.customizations.narrative.channels.dao.ChannelDomainDAO;
import org.narrative.network.customizations.narrative.channels.dao.ChannelUserDAO;
import org.narrative.network.customizations.narrative.channels.dao.DeletedChannelDAO;
import org.narrative.network.customizations.narrative.channels.dao.FollowedChannelDAO;
import org.narrative.network.customizations.narrative.elections.Election;
import org.narrative.network.customizations.narrative.elections.ElectionNominee;
import org.narrative.network.customizations.narrative.elections.dao.ElectionDAO;
import org.narrative.network.customizations.narrative.elections.dao.ElectionNomineeDAO;
import org.narrative.network.customizations.narrative.invoices.FiatPayment;
import org.narrative.network.customizations.narrative.invoices.Invoice;
import org.narrative.network.customizations.narrative.invoices.NrvePayment;
import org.narrative.network.customizations.narrative.invoices.dao.FiatPaymentDAO;
import org.narrative.network.customizations.narrative.invoices.dao.InvoiceDAO;
import org.narrative.network.customizations.narrative.invoices.dao.NrvePaymentDAO;
import org.narrative.network.customizations.narrative.niches.elections.NicheModeratorElection;
import org.narrative.network.customizations.narrative.niches.elections.dao.NicheModeratorElectionDAO;
import org.narrative.network.customizations.narrative.niches.ledgerentries.LedgerEntry;
import org.narrative.network.customizations.narrative.niches.ledgerentries.dao.LedgerEntryDAO;
import org.narrative.network.customizations.narrative.niches.niche.Niche;
import org.narrative.network.customizations.narrative.niches.niche.NicheOfInterest;
import org.narrative.network.customizations.narrative.niches.niche.dao.NicheDAO;
import org.narrative.network.customizations.narrative.niches.niche.dao.NicheOfInterestDAO;
import org.narrative.network.customizations.narrative.niches.nicheassociation.NicheUserAssociation;
import org.narrative.network.customizations.narrative.niches.nicheassociation.dao.NicheUserAssociationDAO;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuction;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionBid;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionInvoice;
import org.narrative.network.customizations.narrative.niches.nicheauction.NicheAuctionSecurityDeposit;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionBidDAO;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionDAO;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionInvoiceDAO;
import org.narrative.network.customizations.narrative.niches.nicheauction.dao.NicheAuctionSecurityDepositDAO;
import org.narrative.network.customizations.narrative.niches.referendum.Referendum;
import org.narrative.network.customizations.narrative.niches.referendum.ReferendumVote;
import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumDAO;
import org.narrative.network.customizations.narrative.niches.referendum.dao.ReferendumVoteDAO;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssue;
import org.narrative.network.customizations.narrative.niches.tribunal.TribunalIssueReport;
import org.narrative.network.customizations.narrative.niches.tribunal.dao.TribunalIssueDAO;
import org.narrative.network.customizations.narrative.niches.tribunal.dao.TribunalIssueReportDAO;
import org.narrative.network.customizations.narrative.personaljournal.PersonalJournal;
import org.narrative.network.customizations.narrative.personaljournal.dao.PersonalJournalDAO;
import org.narrative.network.customizations.narrative.posts.ChannelContent;
import org.narrative.network.customizations.narrative.posts.NarrativePostContent;
import org.narrative.network.customizations.narrative.posts.dao.ChannelContentDAO;
import org.narrative.network.customizations.narrative.posts.dao.NarrativePostContentDAO;
import org.narrative.network.customizations.narrative.publications.Publication;
import org.narrative.network.customizations.narrative.publications.PublicationInvoice;
import org.narrative.network.customizations.narrative.publications.PublicationWaitListEntry;
import org.narrative.network.customizations.narrative.publications.dao.PublicationDAO;
import org.narrative.network.customizations.narrative.publications.dao.PublicationInvoiceDAO;
import org.narrative.network.customizations.narrative.publications.dao.PublicationWaitListEntryDAO;
import org.narrative.network.customizations.narrative.reputation.EventMessage;
import org.narrative.network.customizations.narrative.reputation.UserReputation;
import org.narrative.network.customizations.narrative.reputation.dao.EventMessageDAO;
import org.narrative.network.customizations.narrative.reputation.dao.UserReputationDAO;
import org.narrative.network.shared.daobase.NetworkDAOImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.narrative.common.util.CoreUtils.*;

/**
 * Date: May 29, 2009
 * Time: 3:15:53 PM
 *
 * @author brian
 */
public class PartitionConfig {

    private static final Collection<PartitionDaoConfig<?, ? extends NetworkDAOImpl>> GLOBAL_DAO_CONFIGS = newLinkedList();
    private static final Collection<PartitionDaoConfig<?, ? extends NetworkDAOImpl>> COMPOSITION_DAO_CONFIGS = newLinkedList();

    static {
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AppliedPatch, AppliedPatchDAO>(PartitionType.GLOBAL, AppliedPatchDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AppVersion, AppVersionDAO>(PartitionType.GLOBAL, AppVersionDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<PatchRunnerLock, PatchRunnerLockDAO>(PartitionType.GLOBAL, PatchRunnerLockDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Partition, PartitionDAO>(PartitionType.GLOBAL, PartitionDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Area, AreaDAO>(PartitionType.GLOBAL, AreaDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaStats, AreaStatsDAO>(PartitionType.GLOBAL, AreaStatsDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaUser, AreaUserDAO>(PartitionType.GLOBAL, AreaUserDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaUserStats, AreaUserStatsDAO>(PartitionType.GLOBAL, AreaUserStatsDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaCircle, AreaCircleDAO>(PartitionType.GLOBAL, AreaCircleDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaCircleUser, AreaCircleUserDAO>(PartitionType.GLOBAL, AreaCircleUserDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaCirclePermission, AreaCirclePermissionDAO>(PartitionType.GLOBAL, AreaCirclePermissionDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaResource, AreaResourceDAO>(PartitionType.GLOBAL, AreaResourceDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<EmailAddress, EmailAddressDAO>(PartitionType.GLOBAL, EmailAddressDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<User, UserDAO>(PartitionType.GLOBAL, UserDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserReputation, UserReputationDAO>(PartitionType.GLOBAL, UserReputationDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserAuth, UserAuthDAO>(PartitionType.GLOBAL, UserAuthDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserStats, UserStatsDAO>(PartitionType.GLOBAL, UserStatsDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<FileOnDisk, FileOnDiskDAO>(PartitionType.GLOBAL, FileOnDiskDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<FileOnDiskStats, FileOnDiskStatsDAO>(PartitionType.GLOBAL, FileOnDiskStatsDAO.class));
        //bl: not here.  handled via the DAO's descendents.
        //GLOBAL_DAO_CONFIGS.add(ImageOnDiskDAO.class);

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<PropertySet, PropertySetDAO>(PartitionType.GLOBAL, PropertySetDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Property, PropertyDAO>(PartitionType.GLOBAL, PropertyDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<WalletTransaction, WalletTransactionDAO>(PartitionType.GLOBAL, WalletTransactionDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Wallet, WalletDAO>(PartitionType.GLOBAL, WalletDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NeoWallet, NeoWalletDAO>(PartitionType.GLOBAL, NeoWalletDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NeoTransaction, NeoTransactionDAO>(PartitionType.GLOBAL, NeoTransactionDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NeoTransactionId, NeoTransactionIdDAO>(PartitionType.GLOBAL, NeoTransactionIdDAO.class));

        // bl: everything below here was previously in the realm partition

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaRlm, AreaRlmDAO>(PartitionType.GLOBAL, AreaRlmDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaUserRlm, AreaUserRlmDAO>(PartitionType.GLOBAL, AreaUserRlmDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaCredentials, AreaCredentialsDAO>(PartitionType.GLOBAL, AreaCredentialsDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Content, ContentDAO>(PartitionType.GLOBAL, ContentDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ContentStats, ContentStatsDAO>(PartitionType.GLOBAL, ContentStatsDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<TrendingContent, TrendingContentDAO>(PartitionType.GLOBAL, TrendingContentDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<FutureContent, FutureContentDAO>(PartitionType.GLOBAL, FutureContentDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ModeratedContent, ModeratedContentDAO>(PartitionType.GLOBAL, ModeratedContentDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaPropertySet, AreaPropertySetDAO>(PartitionType.GLOBAL, AreaPropertySetDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<AreaPropertyOverride, AreaPropertyOverrideDAO>(PartitionType.GLOBAL, AreaPropertyOverrideDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<SandboxedAreaUser, SandboxedAreaUserDAO>(PartitionType.GLOBAL, SandboxedAreaUserDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<WatchedUser, WatchedUserDAO>(PartitionType.GLOBAL, WatchedUserDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Portfolio, PortfolioDAO>(PartitionType.GLOBAL, PortfolioDAO.class));

        // jw: NARRATIVE OBJECTS

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Niche, NicheDAO>(PartitionType.GLOBAL, NicheDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheOfInterest, NicheOfInterestDAO>(PartitionType.GLOBAL, NicheOfInterestDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheUserAssociation, NicheUserAssociationDAO>(PartitionType.GLOBAL, NicheUserAssociationDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Referendum, ReferendumDAO>(PartitionType.GLOBAL, ReferendumDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<FollowedChannel, FollowedChannelDAO>(PartitionType.GLOBAL, FollowedChannelDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ReferendumVote, ReferendumVoteDAO>(PartitionType.GLOBAL, ReferendumVoteDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheAuction, NicheAuctionDAO>(PartitionType.GLOBAL, NicheAuctionDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheAuctionBid, NicheAuctionBidDAO>(PartitionType.GLOBAL, NicheAuctionBidDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheAuctionInvoice, NicheAuctionInvoiceDAO>(PartitionType.GLOBAL, NicheAuctionInvoiceDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheAuctionSecurityDeposit, NicheAuctionSecurityDepositDAO>(PartitionType.GLOBAL, NicheAuctionSecurityDepositDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Invoice, InvoiceDAO>(PartitionType.GLOBAL, InvoiceDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NrvePayment, NrvePaymentDAO>(PartitionType.GLOBAL, NrvePaymentDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<FiatPayment, FiatPaymentDAO>(PartitionType.GLOBAL, FiatPaymentDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<TribunalIssue, TribunalIssueDAO>(PartitionType.GLOBAL, TribunalIssueDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<TribunalIssueReport, TribunalIssueReportDAO>(PartitionType.GLOBAL, TribunalIssueReportDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<LedgerEntry, LedgerEntryDAO>(PartitionType.GLOBAL, LedgerEntryDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Election, ElectionDAO>(PartitionType.GLOBAL, ElectionDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ElectionNominee, ElectionNomineeDAO>(PartitionType.GLOBAL, ElectionNomineeDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheModeratorElection, NicheModeratorElectionDAO>(PartitionType.GLOBAL, NicheModeratorElectionDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<EventMessage, EventMessageDAO>(PartitionType.GLOBAL, EventMessageDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<PersonalJournal, PersonalJournalDAO>(PartitionType.GLOBAL, PersonalJournalDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Channel, ChannelDAO>(PartitionType.GLOBAL, ChannelDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ChannelUser, ChannelUserDAO>(PartitionType.GLOBAL, ChannelUserDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ChannelDomain, ChannelDomainDAO>(PartitionType.GLOBAL, ChannelDomainDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ChannelContent, ChannelContentDAO>(PartitionType.GLOBAL, ChannelContentDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<DeletedChannel, DeletedChannelDAO>(PartitionType.GLOBAL, DeletedChannelDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserKyc, UserKycDAO>(PartitionType.GLOBAL, UserKycDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserKycEvent, UserKycEventDAO>(PartitionType.GLOBAL, UserKycEventDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<RoleContentPageView, RoleContentPageViewDAO>(PartitionType.GLOBAL, RoleContentPageViewDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<Publication, PublicationDAO>(PartitionType.GLOBAL, PublicationDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<PublicationInvoice, PublicationInvoiceDAO>(PartitionType.GLOBAL, PublicationInvoiceDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<PublicationWaitListEntry, PublicationWaitListEntryDAO>(PartitionType.GLOBAL, PublicationWaitListEntryDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<RewardPeriod, RewardPeriodDAO>(PartitionType.GLOBAL, RewardPeriodDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NarrativeCompanyReward, NarrativeCompanyRewardDAO>(PartitionType.GLOBAL, NarrativeCompanyRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserTribunalReward, UserTribunalRewardDAO>(PartitionType.GLOBAL, UserTribunalRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserElectorateReward, UserElectorateRewardDAO>(PartitionType.GLOBAL, UserElectorateRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserActivityReward, UserActivityRewardDAO>(PartitionType.GLOBAL, UserActivityRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<UserActivityRewardEvent, UserActivityRewardEventDAO>(PartitionType.GLOBAL, UserActivityRewardEventDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<PublicationReward, PublicationRewardDAO>(PartitionType.GLOBAL, PublicationRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheReward, NicheRewardDAO>(PartitionType.GLOBAL, NicheRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheOwnerReward, NicheOwnerRewardDAO>(PartitionType.GLOBAL, NicheOwnerRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheModeratorReward, NicheModeratorRewardDAO>(PartitionType.GLOBAL, NicheModeratorRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ContentReward, ContentRewardDAO>(PartitionType.GLOBAL, ContentRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<RoleContentReward, RoleContentRewardDAO>(PartitionType.GLOBAL, RoleContentRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<NicheContentReward, NicheContentRewardDAO>(PartitionType.GLOBAL, NicheContentRewardDAO.class));
        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ProratedMonthRevenue, ProratedMonthRevenueDAO>(PartitionType.GLOBAL, ProratedMonthRevenueDAO.class));

        GLOBAL_DAO_CONFIGS.add(new PartitionDaoConfig<ItemHourTrendingStats, ItemHourTrendingStatsDAO>(PartitionType.GLOBAL, ItemHourTrendingStatsDAO.class));
    }

    static {
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<Composition, CompositionDAO>(PartitionType.COMPOSITION, CompositionDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<CompositionStats, CompositionStatsDAO>(PartitionType.COMPOSITION, CompositionStatsDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<CompositionMentions, CompositionMentionsDAO>(PartitionType.COMPOSITION, CompositionMentionsDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<Reply, ReplyDAO>(PartitionType.COMPOSITION, ReplyDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<ReplyMentions, ReplyMentionsDAO>(PartitionType.COMPOSITION, ReplyMentionsDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<ReplyStats, ReplyStatsDAO>(PartitionType.COMPOSITION, ReplyStatsDAO.class));

        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<FilePointer, FilePointerDAO>(PartitionType.COMPOSITION, FilePointerDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<FilePointerSet, FilePointerSetDAO>(PartitionType.COMPOSITION, FilePointerSetDAO.class));

        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<NarrativePostContent, NarrativePostContentDAO>(PartitionType.COMPOSITION, NarrativePostContentDAO.class));

        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<UserQualityRatedComposition, UserQualityRatedCompositionDAO>(PartitionType.COMPOSITION, UserQualityRatedCompositionDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<UserQualityRatedReply, UserQualityRatedReplyDAO>(PartitionType.COMPOSITION, UserQualityRatedReplyDAO.class));
        COMPOSITION_DAO_CONFIGS.add(new PartitionDaoConfig<UserAgeRatedComposition, UserAgeRatedCompositionDAO>(PartitionType.COMPOSITION, UserAgeRatedCompositionDAO.class));

    }

    public static final Map<PartitionType, Collection> PARTITION_DAO_CONFIGS = newHashMap();

    static {
        PARTITION_DAO_CONFIGS.put(PartitionType.GLOBAL, Collections.unmodifiableCollection(GLOBAL_DAO_CONFIGS));
        PARTITION_DAO_CONFIGS.put(PartitionType.COMPOSITION, Collections.unmodifiableCollection(COMPOSITION_DAO_CONFIGS));
    }
}

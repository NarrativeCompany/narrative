import loadable from 'loadable-components';
import { Loading } from '../shared/components/Loading';
import { LoadingViewWrapper } from '../shared/components/LoadingViewWrapper';

// Primary routes
export const Home = loadable(() => import('./Home/Home'), {
  LoadingComponent: LoadingViewWrapper
});
export const Discover = loadable(() => import('./Discover/Discover'), {
  LoadingComponent: LoadingViewWrapper
});
export const Login = loadable(() => import('./Login/LoginForm'), {
  LoadingComponent: Loading
});
export const LoginVerify = loadable(() => import('./Login/LoginVerify'), {
  LoadingComponent: Loading
});
export const ResetPassword = loadable(() => import('./Login/ResetPassword'), {
  LoadingComponent: Loading
});
export const Register = loadable(() => import('./Register/Register'), {
  LoadingComponent: Loading
});
export const ConfirmEmail = loadable(() => import('./Register/ConfirmEmail'), {
  LoadingComponent: Loading
});
export const ConfirmEmailChange = loadable(() => import('./Register/ConfirmEmailChange'), {
  LoadingComponent: Loading
});
export const CancelEmailChange = loadable(() => import('./Register/CancelEmailChange'), {
  LoadingComponent: Loading
});
export const SuspendEmail = loadable(() => import('./Register/SuspendEmail'), {
  LoadingComponent: Loading
});
export const HQLanding = loadable(() => import('./HQ/HQLanding'), {
  LoadingComponent: LoadingViewWrapper
});
export const HQ = loadable(() => import('./HQ/HQ'), {
  LoadingComponent: LoadingViewWrapper
});
export const SuggestNiche = loadable(() => import('./SuggestNiche/SuggestNiche'), {
  LoadingComponent: LoadingViewWrapper
});
export const CreatePublication = loadable(() => import('./CreatePublication/CreatePublication'), {
  LoadingComponent: LoadingViewWrapper
});
export const MemberCP = loadable(() => import('./MemberCP/MemberCP'), {
  LoadingComponent: LoadingViewWrapper
});
export const MemberProfile = loadable(() => import('./MemberProfile/MemberProfile'), {
  LoadingComponent: LoadingViewWrapper
});
export const TribunalAppealDetails = loadable(() => import('./HQ/Appeals/Details/AppealDetails'), {
  LoadingComponent: LoadingViewWrapper
});
export const NicheDetails = loadable(() => import('./Niche/Details/NicheDetails'), {
  LoadingComponent: LoadingViewWrapper
});
export const PublicationLayout = loadable(() => import('./Publication/PublicationLayout'), {
  LoadingComponent: LoadingViewWrapper
});
export const ApprovalDetails = loadable(() => import('./HQ/Approvals/Details/ApprovalDetails'), {
  LoadingComponent: LoadingViewWrapper
});
export const AuctionDetails = loadable(() => import('./HQ/Auctions/Details/AuctionDetails'), {
  LoadingComponent: LoadingViewWrapper
});
export const ModeratorElectionDetails = loadable(() => import('./HQ/Moderators/Details/ModeratorElectionDetails'), {
  LoadingComponent: LoadingViewWrapper
});
export const Post = loadable(() => import('./Post/Post'), {
  LoadingComponent: Loading
});
export const NicheExplainer = loadable(() => import('./About/Niche/NicheExplainer'), {
  LoadingComponent: LoadingViewWrapper
});
export const PublicationExplainer = loadable(() => import('./About/Publication/PublicationExplainer'), {
  LoadingComponent: LoadingViewWrapper
});
export const NRVEExplainer = loadable(() => import('./About/NRVE/NRVEExplainer'), {
  LoadingComponent: LoadingViewWrapper
});
export const RewardsExplainer = loadable(() => import('./About/Rewards/RewardsExplainer'), {
  LoadingComponent: LoadingViewWrapper
});
export const CertificationExplainer = loadable(() => import('./About/Certification/CertificationExplainer'), {
  LoadingComponent: LoadingViewWrapper
});
export const NeoWalletsExplainer = loadable(() => import('./About/NeoWallets/NeoWalletsExplainer'), {
  LoadingComponent: LoadingViewWrapper
});
export const MemberCertificationForm = loadable(() => import('./MemberCertification/MemberCertification'), {
  LoadingComponent: LoadingViewWrapper
});
export const PostDetail = loadable(() => import('./PostDetail/PostDetail'), {
  LoadingComponent: LoadingViewWrapper
});

// Secondary routes
export const Approvals = loadable(() => import('./HQ/Approvals/ApprovalList'));
export const Auctions = loadable(() => import('./HQ/Auctions/Auctions'));
export const AuctionInvoice = loadable(() => import('./HQ/Auctions/Invoice/AuctionInvoice'));
export const TribunalMembers = loadable(() => import('./HQ/Members/TribunalMembersList'));
export const TribunalAppeals = loadable(() => import('./HQ/Appeals/TribunalAppeals'));
export const ModeratorCenter = loadable(() => import('./HQ/Moderators/ModeratorCenter'));
export const NetworkStats = loadable(() => import('./HQ/Reporting/NetworkStats/NetworkStats'));
export const NetworkStatsRewards = loadable(() => import('./HQ/Reporting/Rewards/Rewards'));
export const MemberNotificationSettings = loadable(
  () => import('./MemberCP/MemberNotificationSettings/MemberNotificationSettings')
);
export const MemberEditProfile = loadable(() => import('./MemberCP/MemberEditProfile/EditMemberSettings'));
export const MemberAccountSettings = loadable(() => import('./MemberCP/MemberAccountSettings/EditAccountSettings'));
export const MemberPersonalSettings = loadable(() => import('./MemberCP/MemberPersonalSettings/EditPersonalSettings'));
export const MemberSecuritySettings = loadable(() => import('./MemberCP/MemberSecuritySettings/EditSecuritySettings'));
export const MemberCertification = loadable(() => import('./MemberCP/MemberCertification/MemberCertification'));
export const MemberNeoWallet = loadable(() => import('./MemberCP/MemberNeoWallet/MemberNeoWallet'));
export const MemberManagePosts = loadable(() => import('./MemberCP/MemberPosts/MemberPosts'));
export const MemberReferralDetails = loadable(() => import('./MemberProfile/ReferralProgram/MemberReferralDetails'));
export const MemberChannels = loadable(() => import('./MemberProfile/MemberChannels/MemberChannels'));
export const MemberNiches = loadable(() => import('./MemberProfile/MemberChannels/Niches/MemberNiches'));
export const MemberPublications = loadable(
  () => import('./MemberProfile/MemberChannels/Publications/MemberPublications'));
export const PersonalJournal = loadable(() => import('./MemberProfile/PersonalJournal/PersonalJournal'));
export const MemberActivity = loadable(() => import('./MemberProfile/MemberActivity/MemberActivity'));
export const MemberReputation = loadable(() => import('./MemberProfile/MemberReputation/MemberReputation'));
export const MemberFollows = loadable(() => import('./MemberProfile/MemberFollows/MemberFollows'));
export const MemberRewards = loadable(() => import('./MemberProfile/MemberRewards/MemberRewards'));
export const NicheActivity = loadable(() => import('./Niche/Details/Activity/NicheActivity'));
export const NichePosts = loadable(() => import('./Niche/Details/Posts/NichePosts'));
export const NicheSettings = loadable(() => import('./Niche/Details/Settings/NicheSettings'));
export const NicheProfile = loadable(() => import('./Niche/Details/Profile/NicheProfile'));
export const NicheRewards = loadable(() => import('./Niche/Details/Rewards/NicheRewards'));
export const PublicationPosts = loadable(() => import('./Publication/Details/Posts/PublicationPosts'));
export const PublicationAbout = loadable(() => import('./Publication/Details/About/PublicationAbout'));
export const PublicationInfo = loadable(() => import('./Publication/Details/About/Info/PublicationInfo'));
export const PublicationActivity = loadable(() => import('./Publication/Details/About/Activity/PublicationActivity'));
export const PublicationSearch = loadable(() => import('./Publication/Details/Search/PublicationSearch'));
export const PublicationCP = loadable(() => import('./Publication/CP/PublicationCP'));
export const PublicationSettings = loadable(() => import('./Publication/CP/Settings/PublicationSettings'));
export const PublicationPowerUsers = loadable(() => import('./Publication/CP/PowerUsers/PublicationPowerUsers'));
export const PublicationReviewQueue = loadable(() => import('./Publication/CP/Review/PublicationReviewQueue'));
export const PublicationAccount = loadable(() => import('./Publication/CP/Account/PublicationAccount'));
export const PublicationPostDetail = loadable(() => import('./Publication/Details/Post/PublicationPostDetail'));
export const PublicationInvitation = loadable(() => import('./Publication/Details/Invitation/PublicationInvitation'));

// MemberFollow Sub-routes
export const MemberFollowedItems =
  loadable(() => import('./MemberProfile/MemberFollows/components/MemberFollowedItems'));
export const MemberFollowedNiches =
  loadable(() => import('./MemberProfile/MemberFollows/components/MemberFollowedNiches'));
export const MemberFollowedPublications =
  loadable(() => import('./MemberProfile/MemberFollows/components/MemberFollowedPublications'));
export const MemberFollowedUsers =
  loadable(() => import('./MemberProfile/MemberFollows/components/MemberFollowedUsers'));
export const MemberFollowers =
  loadable(() => import('./MemberProfile/MemberFollows/components/MemberFollowers'));

// MemberRewards Sub-routes
export const MemberRewardsOverview =
  loadable(() => import('./MemberProfile/MemberRewards/components/MemberRewardsOverview'));
export const MemberRewardsTransactions =
  loadable(() => import('./MemberProfile/MemberRewards/components/MemberRewardsTransactions'));

// NicheRewards Sub-routes
export const NicheRewardsOverview =
  loadable(() => import('./Niche/Details/Rewards/components/NicheRewardsOverview'));
export const NicheRewardsLeaderboard =
  loadable(() => import('./Niche/Details/Rewards/components/NicheRewardsLeaderboard'));

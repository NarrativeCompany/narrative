import { defineMessages } from 'react-intl';

export const PostMessages = defineMessages({
  NavBarTitle: {
    id: 'postMessages.headerTitle',
    defaultMessage: 'New Post'
  },
  NavBarTitleEdit: {
    id: 'postMessages.headerTitleEdit',
    defaultMessage: 'Edit Post'
  },
  TitleInputPlaceholder: {
    id: 'postMessages.titleInputPlaceholder',
    defaultMessage: 'Title'
  },
  SubtitleInputPlaceholder: {
    id: 'postMessages.subtitleInputPlaceholder',
    defaultMessage: 'Subtitle (Optional)'
  },
  JournalLabel: {
    id: 'sharedComponentMessages.journalLabel',
    defaultMessage: 'Journal'
  },
  PersonalJournalCheckboxText: {
    id: 'postMessages.personalJournalCheckboxText',
    defaultMessage: 'Post to Journal'
  },
  AgeRatingCheckboxLabel: {
    id: 'postMessages.ageRatingCheckboxLabel',
    defaultMessage: 'Age Rating'
  },
  AgeRatingCheckboxText: {
    id: 'postMessages.ageRatingCheckboxText',
    defaultMessage: 'Content is Intended for Ages 18+'
  },
  AgeRatingCheckboxExtra: {
    id: 'postMessages.ageRatingCheckboxHelperText',
    defaultMessage: 'Protect underage viewers by age-rating your post. Note that an improper age rating will' +
      ' negatively impact your reputation. '
  },
  CommentsCheckboxLabel: {
    id: 'postMessages.commentsCheckboxLabel',
    defaultMessage: 'Comments'
  },
  CommentsCheckboxText: {
    id: 'postMessages.commentsCheckboxText',
    defaultMessage: 'Disable New Comments'
  },
  CanonicalUrlInputLabel: {
    id: 'postMessages.canonicalUrlInputLabel',
    defaultMessage: 'Canonical Link'
  },
  CanonicalUrlInputPlaceholder: {
    id: 'postMessages.canonicalUrlInputPlaceholder',
    defaultMessage: 'Enter URL of original article'
  },
  CanonicalUrlInputDescription: {
    id: 'postMessages.canonicalUrlInputDescription',
    defaultMessage: 'If you previously posted this elsewhere, provide the URL for the original post. Note that posts ' +
      'with canonical links do not qualify for Content Creator rewards. {learnMore}.'
  },
  LearnMore: {
    id: 'postMessages.learnMore',
    defaultMessage: 'Learn More'
  },
  PostBodyPlaceholder: {
    id: 'postMessages.postBodyPlaceholder',
    defaultMessage: 'Write a post...'
  },
  PostBodyErrorMessage: {
    id: 'postMessages.postBodyErrorMessage',
    defaultMessage: 'Post body must be at least 2 characters in length'
  },
  EditPostBtnText: {
    id: 'postMessages.editPostBtnText',
    defaultMessage: 'Edit Post'
  },
  ChannelSelectionTitle: {
    id: 'postMessages.connectedNichesChannelSelection',
    defaultMessage: '1. Select a Channel'
  },
  ChannelSelectionPageTitle: {
    id: 'postMessages.channelSelectionPageTitle',
    defaultMessage: 'Where to Publish'
  },
  ChannelSelectionPageDescription: {
    id: 'postMessages.channelSelectionPageDescription',
    defaultMessage: 'Decide where the post will be displayed.'
  },
  ChannelDescription: {
    id: 'postMessages.connectedNichesChannelDescription',
    defaultMessage: 'You can post to your public {journalLink}, a Publication where you are ' +
      'an authorized Writer, or none. If you choose "none", your post will still be ' +
      'accessible if it is linked to at least one {nicheLink}.'
  },
  ConnectedNichesSectionTitle: {
    id: 'postMessages.connectedNichesPageHeaderTitle',
    defaultMessage: '2. Connect up to 3 Niches'
  },
  ConnectedNichesChannelSelectionLabel: {
    id: 'postMessages.connectedNichesChannelSelectionLabel',
    defaultMessage: 'Publish To This Channel'
  },
  ChannelSelectionDefaultChoiceLabel: {
    id: 'postMessages.channelSelectionDefaultChoiceLabel',
    defaultMessage: 'Choose One'
  },
  PublicationWarning: {
    id: 'postMessages.publicationWarning',
    defaultMessage: 'By posting to this publication, you acknowledge that:'
  },
  PublicationWarningPointOne: {
    id: 'postMessages.publicationWarningPointOne',
    defaultMessage: 'The Publication will have complete control of your post, including the ability to edit it.'
  },
  PublicationWarningPointTwo: {
    id: 'postMessages.publicationWarningPointTwo',
    defaultMessage: 'The Publication may capture all or some of the Content Creator rewards for this post, at its' +
      ' discretion, subject to change any time. Currently, the Publication is taking {publicationPercentage} of' +
      ' Content Creator rewards for their posts.'
  },
  ChannelSelectionYourPublicJournalLabel: {
    id: 'postMessages.channelSelectionYourPublicJournalLabel',
    defaultMessage: 'Your Public Journal'
  },
  ChannelSelectionNoneLabel: {
    id: 'postMessages.channelSelectionNoneLabel',
    defaultMessage: 'None'
  },
  ConnectedNichesSectionDescription: {
    id: 'postMessages.connectedNichesSectionDescription',
    defaultMessage: '{nichesLink} are unique subjects in Narrative. Associating your post to ' +
      'Niches will make your post more discoverable and also make it eligible to earn ' +
      'Narrative Rewards. Search for and choose Niches that match the content of your post.'
  },
  ConnectedNichesSectionDescriptionEditor: {
    id: 'postMessages.connectedNichesSectionDescriptionEditor',
    defaultMessage: '{nichesLink} are unique subjects in Narrative. Associating a post to ' +
      'Niches will make the post more discoverable and also make it eligible to earn ' +
      'Narrative Rewards. Search for and choose Niches that match the content of the post.'
  },
  LearnAboutNichesLink: {
    id: 'postMessages.learnAboutNichesLink',
    defaultMessage: 'Learn more about Niches.'
  },
  ConnectNicheCountText: {
    id: 'postMessages.connectNicheCountText',
    defaultMessage: '{totalCount} {totalCount, plural, one {Niche} other {Niches}} connected. {totalMissing} to go!'
  },
  ConnectNicheCountCompleteText: {
    id: 'postMessages.connectNicheCountCompleteText',
    defaultMessage: 'All 3 Niches connected!'
  },
  PostConfirmationPageHeaderTitle: {
    id: 'postMessages.postConfirmationPageHeaderTitle',
    defaultMessage: 'Your post has been published!'
  },
  PostPendingConfirmationPageHeaderTitle: {
    id: 'postMessages.postPendingConfirmationPageHeaderTitle',
    defaultMessage: 'Your post is pending review!'
  },
  PostConfirmationPendingEditorialApprovalDescription: {
    id: 'postMessages.postConfirmationPendingEditorialApprovalDescription',
    defaultMessage: 'Your post is pending editorial approval by the Publication before it goes live.'
  },
  PostConfirmationPageHeaderDescription: {
    id: 'postMessages.postConfirmationPageHeaderDescription',
    defaultMessage: 'Your post was published to following locations: '
  },
  PostPendingConfirmationPageHeaderDescription: {
    id: 'postMessages.postPendingConfirmationPageHeaderDescription',
    defaultMessage: 'Your post was submitted to following locations: '
  },
  ConnectionLocationLive: {
    id: 'postMessages.connectionLocationLive',
    defaultMessage: 'Live'
  },
  ConnectionLocationPending: {
    id: 'postMessages.connectionLocationPending',
    defaultMessage: 'Pending'
  },
  PostLocationNichesTitle: {
    id: 'postMessages.postLocationNichesTitle',
    defaultMessage: 'Niches ({nicheCount})'
  },
  PostLocationPublicationTitle: {
    id: 'postMessages.postLocationPublicationTitle',
    defaultMessage: 'Publication'
  },
  ContinueToPostBtnText: {
    id: 'postMessages.continueToPostBtnText',
    defaultMessage: 'Continue To My Post'
  },
  PublishBtnText: {
    id: 'postMessages.publishNowBtnText',
    defaultMessage: 'Publish'
  },
  ConnectNichesBtnText: {
    id: 'postMessages.connectNichesBtnText',
    defaultMessage: 'Connect Niches'
  },
  DeletePostConfirmationTitle: {
    id: 'postMessages.deletePostConfirmationTitle',
    defaultMessage: 'Are you sure you want to delete your post?'
  },
  SaveAndExitConfirmationTitle: {
    id: 'postMessages.saveAndExitConfirmationTitle',
    defaultMessage: 'Are you sure you want to exit?'
  },
  DiscardAndExitConfirmationTitle: {
    id: 'postMessages.discardAndExitConfirmationTitle',
    defaultMessage: 'Are you sure you want to discard your changes?'
  },
  SaveAndExitBtnText: {
    id: 'postMessages.saveAndExitBtnText',
    defaultMessage: 'Save and Exit'
  },
  DiscardAndExitBtnText: {
    id: 'postMessages.discardAndExitBtnText',
    defaultMessage: 'Cancel Changes'
  },
  DeleteAndExitBtnText: {
    id: 'postMessages.deleteAndExitBtnText',
    defaultMessage: 'Delete and Exit'
  },
  DeleteBtnText: {
    id: 'postMessages.delete',
    defaultMessage: 'Delete'
  },
  ViewPostBtnText: {
    id: 'postMessages.viewPostBtnText',
    defaultMessage: 'View Post'
  },
  ViewPendingPostBtnText: {
    id: 'postMessages.viewPendingPostBtnText',
    defaultMessage: 'View Your Pending Posts'
  },
  ApproveThisPostNowButtonText: {
    id: 'postMessages.approveThisPostNowButtonText',
    defaultMessage: 'Approve This Post Now'
  },
  SavedToDrafts: {
    id: 'postMessages.savedToDrafts',
    defaultMessage: 'Saved to drafts'
  },
  LivePostAlertMessage: {
    id: 'postMessages.livePostAlertMessage',
    defaultMessage: 'You are editing a live post. Auto save has been disabled.'
  },
  AutoSaveDisabledAlertMessage: {
    id: 'postMessages.autoSaveDisabledAlertMessage',
    defaultMessage: 'Auto save has been disabled.'
  },
  BeforeUnloadReturnMessage: {
    id: 'postMessages.beforeUnloadReturnMessage',
    defaultMessage: 'Are you sure you want to exit? Unsaved changes will be lost.'
  },
  UploadImageErrorTitle: {
    id: 'postMessages.uploadImageErrorTitle',
    defaultMessage: 'Error Uploading Image'
  },
  UploadImageErrorMessage: {
    id: 'postMessages.uploadImageErrorMessage',
    defaultMessage: 'There was an error uploading your image. Please go back and try again.'
  },
  NicheSearchPlaceholder: {
    id: 'postMessages.nicheSearchPlaceholder',
    defaultMessage: 'Search for Niches to link...'
  },
  AlreadyBlockedByNiche: {
    id: 'postMessages.alreadyBlockedByNiche',
    defaultMessage: 'This post was was previously removed from {nicheLink}, and cannot be published there again.'
  },
});

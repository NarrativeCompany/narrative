import { defineMessages } from 'react-intl';

export const SuspendEmailMessages = defineMessages({
  PageTitle: {
    id: 'suspendEmail.pageTitle',
    defaultMessage: 'Suspend All Email Notifications'
  },
  SuspendEmailsDescription: {
    id: 'suspendEmail.suspendEmailsDescription',
    defaultMessage: 'Are you sure you want to stop receiving all email notifications for {userLink} ({emailAddress})?'
  },
  SuspendEmailsConfirmButtonText: {
    id: 'suspendEmailButtons.suspendEmailsConfirmButtonText',
    defaultMessage: 'Suspend All Now'
  },
  SuspendEmailsCancelButtonText: {
    id: 'suspendEmailButtons.suspendEmailsCancelButtonText',
    defaultMessage: 'Cancel'
  },
  EmailsSuspended: {
    id: 'suspendEmailButtons.emailsSuspended',
    defaultMessage: 'You have successfully unsubscribed from future emails to: {emailAddress}'
  },
});

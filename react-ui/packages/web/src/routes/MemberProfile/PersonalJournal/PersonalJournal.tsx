import * as React from 'react';
import { compose, withProps } from 'recompose';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../shared/context/MemberProfileContext';
import {
  ContentStreamChannelType,
  withChannelContentStream,
} from '@narrative/shared';
import {
  withContentStreamSorts,
  WithContentStreamSortsProps
} from '../../../shared/containers/withContentStreamSorts';
import { WebRoute } from '../../../shared/constants/routes';
import {
  ContentStream,
  WithContentStreamPropsFromQuery,
  withContentStreamPropsFromQuery
} from '../../../shared/components/contentStream/ContentStream';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { SEO } from '../../../shared/components/SEO';
import { PersonalJournalMessages } from '../../../shared/i18n/PersonalJournalMessages';
import { PillMenu } from '../../../shared/components/navigation/pills/PillMenu';

type Props =
  WithMemberProfileProps &
  WithContentStreamPropsFromQuery &
  WithContentStreamSortsProps &
  InjectedIntlProps;

const PersonalJournalComponent: React.SFC<Props> = (props) => {
  const {
    intl: {formatMessage},
    isForCurrentUser,
    contentStreamProps,
    pillMenuProps,
    detailsForProfile: {user: {displayName}}
  } = props;
  const descriptionMessage = isForCurrentUser
    ? PersonalJournalMessages.PageHeaderDescriptionForCurrentUser
    : PersonalJournalMessages.PageHeaderDescription;

  return (
    <React.Fragment>
      <SEO
        title={formatMessage(PersonalJournalMessages.PageHeaderTitle, {displayName})}
        description={descriptionMessage}
      />

      <PillMenu {...pillMenuProps} />

      <ContentStream {...contentStreamProps}/>
    </React.Fragment>
  );
};

export const PersonalJournal = compose(
  MemberProfileConnect,
  withProps((props: WithMemberProfileProps) => {
    const { detailsForProfile: { user } } = props;
    const { username } = user;

    return {
      baseParameters: { username },
      channelType: ContentStreamChannelType.personal_journals,
      channelOid: user.oid
    };
  }),
  withContentStreamSorts(WebRoute.UserProfile, WebRoute.UserProfileJournal),
  withChannelContentStream,
  // jw: now, let's make those search results easy to consume.
  withContentStreamPropsFromQuery(),
  injectIntl
)(PersonalJournalComponent) as React.ComponentClass<{}>;

export default PersonalJournal;

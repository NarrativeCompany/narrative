import * as React from 'react';
import { User } from '@narrative/shared';
import { Link } from '../../../../../shared/components/Link';
import { FormattedMessage } from 'react-intl';
import { MemberChannelsMessages } from '../../../../../shared/i18n/MemberChannelsMessages';
import { SectionHeader } from '../../../../../shared/components/SectionHeader';

interface Props {
  forCurrentUser: boolean;
  user: User;
}

export const MemberPublicationsDescription: React.SFC<Props> = (props) => {
  const { forCurrentUser, user } = props;

  const publicationsLink = <Link.About type="publications" />;

  if (forCurrentUser) {
    return (
      <SectionHeader title={
        <FormattedMessage {...MemberChannelsMessages.PublicationsYouAreAssociatedWith} values={{publicationsLink}}/>
      }/>
    );
  }

  const { displayName } = user;

  return (
    <SectionHeader title={
      <FormattedMessage
        {...MemberChannelsMessages.PublicationsMemberAssociatedWith}
        values={{publicationsLink, displayName}}
      />
    }/>
  );
};

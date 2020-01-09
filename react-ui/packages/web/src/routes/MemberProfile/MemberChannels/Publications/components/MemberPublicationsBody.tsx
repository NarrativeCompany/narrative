import * as React from 'react';
import { compose } from 'recompose';
import {
  withExtractedUserOidFromMemberProfileProps
} from '../../../../../shared/containers/withExtractedUserOidFromMemberProfileProps';
import { withPublicationUserAssociations, WithPublicationUserAssociationsProps } from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../../shared/utils/withLoadingPlaceholder';
import { MemberProfileConnect, WithMemberProfileProps } from '../../../../../shared/context/MemberProfileContext';
import { Link } from '../../../../../shared/components/Link';
import { MemberChannelsMessages } from '../../../../../shared/i18n/MemberChannelsMessages';
import { MemberPublicationsDescription } from './MemberPublicationsDescription';
import { MemberAssociatedPublication } from './MemberAssociatedPublication';
import { NoResultsMessage } from '../../../../../shared/components/NoResultsMessage';

type Props = Pick<WithPublicationUserAssociationsProps, 'associations'> &
  WithMemberProfileProps;

const MemberPublicationsBodyComponent: React.SFC<Props> = (props) => {
  const { associations, isForCurrentUser, detailsForProfile: { user } } = props;

  // jw: if we do not have any associations let's just put out a default message to that affect.
  if (!associations.length) {
    const publicationsLink = <Link.About type="publications" />;

    if (isForCurrentUser) {
      return (
        <NoResultsMessage
          message={MemberChannelsMessages.YouHaveNoPublicationAssociations}
          values={{publicationsLink}}
        />
      );
    }

    const { displayName } = user;

    return (
      <NoResultsMessage
        message={MemberChannelsMessages.MemberHasNoPublicationAssociations}
        values={{publicationsLink, displayName}}
      />
    );
  }

  return (
    <React.Fragment>
      <MemberPublicationsDescription user={user} forCurrentUser={isForCurrentUser} />

      {associations.map(association =>
        <MemberAssociatedPublication
          key={association.publication.oid}
          association={association}
        />
      )}
    </React.Fragment>
  );
};

export const MemberPublicationsBody = compose(
  MemberProfileConnect,
  withExtractedUserOidFromMemberProfileProps,
  withPublicationUserAssociations,
  withLoadingPlaceholder(fullPlaceholder),
)(MemberPublicationsBodyComponent) as React.ComponentClass<{}>;

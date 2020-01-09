import * as React from 'react';
import { compose } from 'recompose';
import {
  withNicheUserAssociations,
  WithNicheUserAssociationsProps
} from '@narrative/shared';
import { MemberNichesCurrentUserIntro } from './MemberNichesCurrentUserIntro';
import { MemberNicheLists } from './MemberNicheLists';
import { WithMemberProfileProps } from '../../../../../shared/context/MemberProfileContext';
import {
  withExtractedUserOidFromMemberProfileProps
} from '../../../../../shared/containers/withExtractedUserOidFromMemberProfileProps';
import {
  fullPlaceholder,
  withLoadingPlaceholder
} from '../../../../../shared/utils/withLoadingPlaceholder';
import { MemberProfileHeaderText } from '../../../MemberProfile';

type Props =
  Pick<WithNicheUserAssociationsProps, 'associations'> &
  WithMemberProfileProps;

const MemberNichesDetailsComponent: React.SFC<Props> = (props) => {
  const { associations, detailsForProfile, isForCurrentUser } = props;

  return (
    <React.Fragment>
      {isForCurrentUser &&
        <MemberProfileHeaderText>
          <MemberNichesCurrentUserIntro nicheCount={associations.length} />
        </MemberProfileHeaderText>
      }

      <MemberNicheLists associations={associations} user={detailsForProfile.user} isCurrentUser={isForCurrentUser} />
    </React.Fragment>
  );
};

export const MemberNichesDetails = compose(
  withExtractedUserOidFromMemberProfileProps,
  withNicheUserAssociations,
  withLoadingPlaceholder(fullPlaceholder)
)(MemberNichesDetailsComponent) as React.ComponentClass<WithMemberProfileProps>;

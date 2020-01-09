import * as React from 'react';
import { compose, withProps } from 'recompose';
import styled from '../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { TribunalIssue } from '@narrative/shared';
import { TribunalAppealCardVoteTimer } from './components/TribunalAppealCardVoteTimer';
import { TribunalAppealCardMembersVotedMessage } from './TribunalAppealCardMembersVotedMessage';
import { TribunalAppealCardYourVoteInfo } from './TribunalAppealCardYourVoteInfo';
import { getApprovalPercentage } from '../../../shared/utils/referendumUtils';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';
import { isPermissionGranted } from '../../../shared/containers/withPermissionsModalController';

const CardFooter = styled<FlexContainerProps>(FlexContainer)`
  padding: 10px 32px 10px 32px;
  width: 100%;
  
  @media screen and (max-width: 576px) {
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 10px 12px 10px 12px;
  }
`;

interface WithProps {
  votePercentage?: number;
  loggedInUserIsTribunal: boolean;
}

interface ParentProps {
  issue: TribunalIssue;
}

type Props =
  WithProps &
  ParentProps &
  WithExtractedCurrentUserProps;

const TribunalAppealCardFooterComponent: React.SFC<Props> = (props) => {
  const { issue, votePercentage, loggedInUserIsTribunal } = props;

  return (
    <CardFooter>
      <TribunalAppealCardVoteTimer tribunalIssue={issue}/>

      <TribunalAppealCardMembersVotedMessage
        votePercentage={votePercentage}
        type={issue.type}
      />

      {loggedInUserIsTribunal &&
      <TribunalAppealCardYourVoteInfo appeal={issue}/>}
    </CardFooter>
  );
};

export const TribunalAppealCardFooter = compose(
  withExtractedCurrentUser,
  withProps((props: Props) => {
    const { issue, currentUserGlobalPermissions } = props;

    const { referendum } = issue;
    const { votePointsFor, votePointsAgainst } = referendum;

    const votePercentage = !parseFloat(votePointsFor) && !parseFloat(votePointsAgainst)
      ? undefined
      : getApprovalPercentage(votePointsFor, votePointsAgainst);
    const loggedInUserIsTribunal = isPermissionGranted('participateInTribunalActions', currentUserGlobalPermissions);

    return { votePercentage, loggedInUserIsTribunal };
  })
)(TribunalAppealCardFooterComponent) as React.ComponentClass<ParentProps>;

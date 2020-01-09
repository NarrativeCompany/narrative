import * as React from 'react';
import styled from '../../../shared/styled/index';
import { ApprovalTimeRemaining } from './ApprovalTimeRemaining';
import { ApprovalRating } from './ApprovalRating';
import { ApprovalTotalComments } from './ApprovalTotalComments';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { Referendum } from '@narrative/shared';

const ActionsWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: 100%;
`;

interface ParentProps {
  referendum: Referendum;
  approvalRating: number;
}

export const ApprovalCardActions: React.SFC<ParentProps> = (props) => {
  const { referendum, approvalRating } = props;
  return (
    <ActionsWrapper>
      <ApprovalTimeRemaining endTime={referendum.endDatetime}/>
      <ApprovalRating approvalRating={approvalRating}/>
      <ApprovalTotalComments
        referendumOid={referendum.oid}
        totalComments={referendum.commentCount}
      />
    </ActionsWrapper>
  );
};

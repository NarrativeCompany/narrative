import * as React from 'react';
import styled from '../../styled';
import { Progress } from 'antd';
import { themeColors } from '../../styled/theme';
import { getApprovalPercentage } from '../../utils/referendumUtils';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Paragraph } from '../Paragraph';

const ProgressContainer = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 20px;
`;

interface Props {
  votePointsFor: string;
  votePointsAgainst: string;
}

export const ReferendumProgressBar: React.SFC<Props> = (props) => {
  const { votePointsFor, votePointsAgainst } = props;

  const approvalPercentage = getApprovalPercentage(votePointsFor, votePointsAgainst);

  const VOTE_THRESHOLD = 50;

  return (
    <ProgressContainer centerAll={true}>
      <Progress
        percent={approvalPercentage}
        strokeWidth={15}
        strokeColor={approvalPercentage <= VOTE_THRESHOLD ? themeColors.primaryRed : themeColors.secondaryBlue}
        showInfo={false}
        style={{marginRight: '10px'}}
      />

      <Paragraph size="small" color="light">
        {approvalPercentage}%
      </Paragraph>
    </ProgressContainer>
  );
};

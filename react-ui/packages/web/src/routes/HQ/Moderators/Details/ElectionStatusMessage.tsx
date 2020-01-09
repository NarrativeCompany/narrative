import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import { Heading } from '../../../../shared/components/Heading';
import { FormattedMessage } from 'react-intl';
import { ElectionStateMessageMessages } from '../../../../shared/i18n/ElectionStageMessageMessages';
import { Niche } from '@narrative/shared';
import { ElectionStatus } from '@narrative/shared';
import styled from '../../../../shared/styled';

const ElectionMessageWrapper = styled<FlexContainerProps>(FlexContainer)`
   margin: 20px 0 40px;

   h3 {
     position: relative;
     color: #fff;
     
     div {
       font-weight: 500;
       display: inline-block;
     }
   }
`;

interface ParentProps {
  niche?: Niche;
  electionStatus?: ElectionStatus;
}

export const ElectionStatusMessage: React.SFC<ParentProps> = (props) => {
  const { niche, electionStatus } = props;

  if (!niche || !electionStatus) {
    return null;
  }

  return (
    <ElectionMessageWrapper centerAll={true}>
      <Heading size={3} noMargin={true} weight={300}>
        <FormattedMessage
          {...getElectionMessageByStatus(electionStatus)}
          values={{ nicheName: <div>{niche.name}</div> }}
        />
      </Heading>
    </ElectionMessageWrapper>
  );
};

function getElectionMessageByStatus (status: ElectionStatus): FormattedMessage.MessageDescriptor {
  switch (status) {
    case ElectionStatus.NOMINATING:
      return ElectionStateMessageMessages.BeforeElection;
    case ElectionStatus.VOTING:
      return ElectionStateMessageMessages.DuringElection;
    case ElectionStatus.COMPLETED:
      return ElectionStateMessageMessages.AfterElection;
    default:
      throw new Error('getElectionMessageByStage: missing election stage');
  }
}

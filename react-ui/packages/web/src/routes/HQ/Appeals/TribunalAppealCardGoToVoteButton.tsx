import * as React from 'react';
import { WebRoute } from '../../../shared/constants/routes';
import { Button } from '../../../shared/components/Button';
import { TribunalIssueType } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { TribunalAppealCardVoteButtonMessages } from '../../../shared/i18n/TribunalAppealCardVoteButtonMessages';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { generatePath } from 'react-router';
import styled from '../../../shared/styled/index';

const ButtonsWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: auto;
  height: 100%;
  margin-left: auto;
  
  @media screen and (max-width: 576px) {
    width: 100%;
    justify-content: center;
  }
`;

interface ParentProps {
  tribunalIssueOid: string;
  type: TribunalIssueType;
}

export const TribunalAppealCardGoToVoteButton: React.SFC<ParentProps> = (props) => {
  const { tribunalIssueOid, type } = props;

  let buttonText;

  switch (type) {
    case TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE:
      buttonText = <FormattedMessage {...TribunalAppealCardVoteButtonMessages.VoteOnEditRequestButtonText} />;
      break;
    case TribunalIssueType.RATIFY_NICHE:
    case TribunalIssueType.RATIFY_PUBLICATION:
    case TribunalIssueType.APPROVE_REJECTED_NICHE:
    default:
      buttonText = <FormattedMessage {...TribunalAppealCardVoteButtonMessages.VoteOnAppealButtonText} />;
      break;
  }

  return (
    <ButtonsWrapper centerAll={true}>
      <Button
        style={{width: 'inherit'}}
        type="ghost"
        size="large"
        href={generatePath(WebRoute.AppealDetails, {tribunalIssueOid})}
      >
        {buttonText}
      </Button>
    </ButtonsWrapper>
  );
};

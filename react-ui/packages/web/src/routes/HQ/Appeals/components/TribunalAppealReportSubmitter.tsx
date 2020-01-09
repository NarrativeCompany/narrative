import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import styled from '../../../../shared/styled';
import { TribunalAppealCardMessages } from '../../../../shared/i18n/TribunalAppealCardMessages';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import { User } from '@narrative/shared';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { MemberAvatar } from '../../../../shared/components/user/MemberAvatar';

const AppealerWrapper = styled<FlexContainerProps>(FlexContainer)`
  align-items: center;
  margin-bottom: 10px;
`;

const MessageWrapper = styled.span`
  margin-left: 6px;
`;

const AvatarWrapper = styled<FlexContainerProps>(FlexContainer)`
  align-items: center;
  justify-content: center;
`;

interface ParentProps {
  user: User;
  creationDatetime: string;
}

export const TribunalAppealReportSubmitter: React.SFC<ParentProps> = (props) => {
  const { user, creationDatetime } = props;

  return (
    <AppealerWrapper>
      <AvatarWrapper>
        <MemberAvatar user={user}/>
      </AvatarWrapper>
      <MessageWrapper>
        <FormattedMessage
          {...TribunalAppealCardMessages.AppealSubmittedByUserOnDateText}
          values={{
            user: user.displayName,
            date: <LocalizedTime time={creationDatetime} dateOnly={true}/>
          }}
        />
      </MessageWrapper>
    </AppealerWrapper>
  );
};

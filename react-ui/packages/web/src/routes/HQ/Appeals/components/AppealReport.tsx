import * as React from 'react';
import { TribunalIssueReport } from '@narrative/shared';
import styled from '../../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import { FormattedMessage } from 'react-intl';
import { TribunalAppealCardMessages } from '../../../../shared/i18n/TribunalAppealCardMessages';
import { MemberLink } from '../../../../shared/components/user/MemberLink';
import { LocalizedTime } from '../../../../shared/components/LocalizedTime';
import { MemberAvatar } from '../../../../shared/components/user/MemberAvatar';
import { commentBodyStyles } from '../../../../shared/styled/shared/comment';

const ReporterWrapper = styled<FlexContainerProps>(FlexContainer)`
  align-items: center;
  margin-bottom: 10px;
`;

const ReporterDetailsWrapper = styled.span`
  margin-left: 6px;
`;

const AvatarWrapper = styled<FlexContainerProps>(FlexContainer)`
  align-items: center;
  justify-content: center;
`;

const ReportCommentContainer = styled.div`
  border-radius: 10px;
  color: #40a9ff;
  padding: 10px;
  
  // jw: generated and then tweaked from: http://www.cssarrowplease.com/ 
  position: relative;
  background: #F2FBFD;
  border: 1px solid #40a9ff;

  :after, :before {
    bottom: 100%;
    left: 25px;
    border: solid transparent;
    content: " ";
    height: 0;
    width: 0;
    position: absolute;
    pointer-events: none;
  }
  
  :after {
    border-color: rgba(242, 251, 253, 0);
    border-bottom-color: #F2FBFD;
    border-width: 10px;
    margin-left: -10px;
  }
  :before {
    border-color: rgba(43, 208, 244, 0);
    border-bottom-color: #40a9ff;
    border-width: 11px;
    margin-left: -11px;
  }
  
  ${commentBodyStyles};
`;

interface Props {
  report: TribunalIssueReport;
}

export const AppealReport: React.SFC<Props> = (props) => {
  const { reporter, creationDatetime, comments } = props.report;

  const user = <MemberLink user={reporter} />;
  const date = <LocalizedTime time={creationDatetime} dateOnly={true} />;

  return (
    <React.Fragment>
      {/* jw: first, let's output the reporter! */}
      <ReporterWrapper>
        <AvatarWrapper>
          <MemberAvatar user={reporter}/>
        </AvatarWrapper>
        <ReporterDetailsWrapper>
          <FormattedMessage {...TribunalAppealCardMessages.AppealSubmittedByUserOnDateText} values={{user, date}} />
        </ReporterDetailsWrapper>
      </ReporterWrapper>

      {/* jw: next: let's output the comment, if there was one. */}
      {comments &&
        <FlexContainer>
          <ReportCommentContainer dangerouslySetInnerHTML={{__html: comments}} />
        </FlexContainer>
      }

    </React.Fragment>
  );
};

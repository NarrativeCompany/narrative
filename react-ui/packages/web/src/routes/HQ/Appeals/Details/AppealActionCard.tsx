import * as React from 'react';
import {
  TribunalIssueReport,
  TribunalIssueDetail,
  TribunalIssueType
} from '@narrative/shared';
import { DetailsActionCard } from '../../../../shared/components/detailAction/DetailsActionCard';
import { FormattedMessage } from 'react-intl';
import { AppealDetailsMessages } from '../../../../shared/i18n/AppealDetailsMessages';
import { List } from 'antd';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { AppealReport } from '../components/AppealReport';
import { AppealNicheEditDetails } from './AppealNicheEditDetails';
import { compose, withProps } from 'recompose';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../../shared/containers/withExtractedCurrentUser';
import { ReferendumVoteDescription } from '../../../../shared/components/referendum/ReferendumVoteDescription';
import { Heading } from '../../../../shared/components/Heading';
import { AppealVoteButtons } from './AppealVoteButtons';
import { TribunalAppealCardVoteTimer } from '../components/TribunalAppealCardVoteTimer';
import { isPermissionGranted } from '../../../../shared/containers/withPermissionsModalController';

interface ParentProps {
  issueDetails: TribunalIssueDetail;
}

interface Props extends ParentProps {
  isTribunalMember: boolean;
}

const AppealActionCardComponent: React.SFC<Props> = (props) => {
  const { isTribunalMember, issueDetails } = props;
  const { tribunalIssue, tribunalIssueReports } = issueDetails;

  const isNicheEdit = tribunalIssue.type === TribunalIssueType.APPROVE_NICHE_DETAIL_CHANGE;
  const titleMessage = isNicheEdit ? AppealDetailsMessages.VoteOnEditRequest : AppealDetailsMessages.VoteOnAppeal;

  return (
    <DetailsActionCard
      title={<FormattedMessage {...titleMessage} />}
      sideColor={isNicheEdit ? 'green' : 'orange'}
      countDown={<TribunalAppealCardVoteTimer tribunalIssue={tribunalIssue} />}>

      {/* jw: first, let's include all reports in order at the top */}
      <List
        dataSource={tribunalIssueReports}
        renderItem={(report: TribunalIssueReport) => (
          <List.Item key={report.oid}>
            <FlexContainer column={true}>
              <AppealReport report={report} />
            </FlexContainer>
          </List.Item>
        )}
      />

      {/* jw: now it's time for the meat and potatoes. Output the details / voting buttons */}
      {isNicheEdit ? (
        <AppealNicheEditDetails
          tribunalIssue={tribunalIssue}
          isTribunalMember={isTribunalMember}
        />
      )
      : (
        <AppealVoteButtons
          tribunalIssue={tribunalIssue}
          isTribunalMember={isTribunalMember}
        />
      )}

      {/* jw: let's only include the current vote if you can participate */}
      {isTribunalMember &&
      <Heading textAlign="center" size={4} style={{marginTop: '15px'}}>
        <ReferendumVoteDescription referendum={tribunalIssue.referendum}/>
      </Heading>}

    </DetailsActionCard>
  );
};

export const AppealActionCard = compose(
  withExtractedCurrentUser,
  withProps((props: WithExtractedCurrentUserProps & ParentProps) => {
    const { currentUserGlobalPermissions } = props;

    const isTribunalMember = isPermissionGranted('participateInTribunalActions', currentUserGlobalPermissions);

    return { isTribunalMember };
  })
)(AppealActionCardComponent) as React.ComponentClass<ParentProps>;

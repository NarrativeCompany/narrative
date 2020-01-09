import * as React from 'react';
import { Col, Row } from 'antd';
import { FormattedMessage } from 'react-intl';
import { SectionHeader } from '../../../shared/components/SectionHeader';
import { ReputationBreakdownCard } from './ReputationBreakdownCard';
import { Link } from '../../../shared/components/Link';
import { MemberReputationMessages } from '../../../shared/i18n/MemberReputationMessages';
import { WithUserReputationProps } from './MemberReputation';
import { WebRoute } from '../../../shared/constants/routes';

export const ReputationBreakdown: React.SFC<WithUserReputationProps> = (props) => {
  const { userReputation, isForCurrentUser } = props;

  const certifiedLink = (
    <Link to={WebRoute.MemberCertification}>
      <FormattedMessage {...MemberReputationMessages.KycCertifiedLink}/>
    </Link>
  );

  const descriptionMessage = isForCurrentUser ?
    MemberReputationMessages.ReputationBreakdownSectionDescriptionForCurrentUser :
    MemberReputationMessages.ReputationBreakdownSectionDescription;

  const qualityDescriptionMessage = isForCurrentUser ?
    MemberReputationMessages.QualityAnalysisDescriptionForCurrentUser :
    MemberReputationMessages.QualityAnalysisDescription;

  const certifiedDescriptionMessage = isForCurrentUser ?
    MemberReputationMessages.CertifiedDescriptionForCurrentUser :
    MemberReputationMessages.CertifiedDescription;

  return (
    <React.Fragment>
      <SectionHeader
        title={<FormattedMessage {...MemberReputationMessages.ReputationBreakdownSectionTitle}/>}
        description={<FormattedMessage {...descriptionMessage}/>}
      />

      <Row gutter={36} type="flex" align="middle" justify="center">

        <Col sm={24} md={24} lg={24}>

          <ReputationBreakdownCard
            color="primary-blue"
            title={MemberReputationMessages.ConductStatusTitle}
            description={MemberReputationMessages.ConductStatusDescription}
            rating={!userReputation.conductNegative ? 100 : 0}
          />

          <ReputationBreakdownCard
            color="bright-green"
            title={MemberReputationMessages.QualityAnalysisTitle}
            description={qualityDescriptionMessage}
            rating={userReputation.qualityAnalysisScore}
          />

          <ReputationBreakdownCard
            color="dark-blue"
            title={MemberReputationMessages.CertifiedTitle}
            description={{ ...certifiedDescriptionMessage, values: { certifiedLink } }}
            rating={userReputation.kycVerifiedScore}
          />

        </Col>
      </Row>
    </React.Fragment>
  );
};

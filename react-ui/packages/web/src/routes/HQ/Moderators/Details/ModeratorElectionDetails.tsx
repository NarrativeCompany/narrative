import * as React from 'react';
import { compose, withProps } from 'recompose';
import { RouteComponentProps } from 'react-router';
import { FormattedMessage } from 'react-intl';
import { DetailsViewWrapper } from '../../../../shared/components/DetailsViewWrapper';
import { SEO } from '../../../../shared/components/SEO';
import { PageHeader } from '../../../../shared/components/PageHeader';
import { Card, CardProps } from '../../../../shared/components/Card';
import { Link } from '../../../../shared/components/Link';
import { ElectionStatusMessage } from './ElectionStatusMessage';
import { ModeratorElectionStatsSection } from './ModeratorElectionStatsSection';
import { ModeratorNomineesSection } from './ModeratorNomineesSection';
import { NicheSection } from './NicheSection';
import { NotFound } from '../../../../shared/components/NotFound';
import { WebRoute } from '../../../../shared/constants/routes';
import { withNicheModeratorElectionDetails, WithNicheModeratorElectionDetailsProps } from '@narrative/shared';
import { SEOMessages } from '../../../../shared/i18n/SEOMessages';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';
import styled from '../../../../shared/styled';
import { DetailsGradientBox } from '../../../../shared/components/DetailsGradientBox';
import { viewWrapperPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';

const ContentCard = styled<CardProps>(Card)`
  width: 100%;
  
  @media screen and (max-width: 539px) {
    .ant-card-body {
      padding: 15px;
    }
  }
`;

type Props =
  WithNicheModeratorElectionDetailsProps;

const ModeratorElectionDetails: React.SFC<Props> = (props) => {
  const { niche, election, electionOid, nomineeCount, currentUserNominee } = props;

  // leaving this here as it will be added into the PageHeader in the iteration
  // const extra = <Paragraph color="success">Election begins in <strong>2d 1m 32s</strong></Paragraph>;

  if (!election) {
    return <NotFound/>;
  }

  return (
    <DetailsViewWrapper gradientBoxColor="black">
      <SEO
        title={SEOMessages.ModeratorElectionTitle}
        description={SEOMessages.ModeratorElectionDescription}
      />

      <ElectionStatusMessage niche={niche} electionStatus={election.status}/>

      <ContentCard>
        <PageHeader
          iconType="election"
          preTitle={<FormattedMessage {...ModeratorElectionDetailsMessages.PageHeaderPreTitle}/>}
          title={<FormattedMessage {...ModeratorElectionDetailsMessages.PageHeaderTitle}/>}
          titleHelper={(
            <Link to={WebRoute.ModeratorElections}>
              <FormattedMessage {...ModeratorElectionDetailsMessages.PageHeaderTitleHelper}/>
            </Link>
          )}
          description={<FormattedMessage{...ModeratorElectionDetailsMessages.PageHeaderDescription}/>}
        />

        <NicheSection niche={niche}/>

        <ModeratorElectionStatsSection election={election}/>

        <ModeratorNomineesSection
          electionOid={electionOid}
          totalNominees={nomineeCount}
          currentUserNominee={currentUserNominee}
        />

      </ContentCard>
    </DetailsViewWrapper>
  );
};

export default compose(
  withProps((props: RouteComponentProps<{electionOid: string}>) => ({
    electionOid: props.match.params.electionOid
  })),
  withNicheModeratorElectionDetails,
  withLoadingPlaceholder(viewWrapperPlaceholder(() => <DetailsGradientBox color="black"/>))
)(ModeratorElectionDetails) as React.ComponentClass<{}>;

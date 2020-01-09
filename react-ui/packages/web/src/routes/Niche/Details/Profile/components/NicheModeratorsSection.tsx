import * as React from 'react';
import { compose, withProps } from 'recompose';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { withNicheModeratorSlots, WithNicheModeratorSlotsProps } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { ContainedLoading } from '../../../../../shared/components/Loading';
import { NicheProfileMessages } from '../../../../../shared/i18n/NicheProfileMessages';
import { WebRoute } from '../../../../../shared/constants/routes';
import { Link } from '../../../../../shared/components/Link';
import { generatePath } from 'react-router';
import {
  generateOpenPositionPlaceholders
} from '../../../../../shared/components/moderatorElection/OpenModeratorPositionPlaceholder';
import { Col, Row } from 'antd';
import { ChannelDetailsSection } from '../../../../../shared/components/channel/ChannelDetailsSection';

// jw: there are a few places where we need to generate this exact link from the same source data!
export function getViewActiveElectionLink(props: WithNicheModeratorSlotsProps): React.ReactNode | undefined {
  const { electionIsLive, activeModeratorElection } = props;

  if (!electionIsLive) {
    return undefined;
  }
  const electionOid = activeModeratorElection.oid;
  const electionUrl = generatePath(WebRoute.ModeratorElectionDetails, {electionOid});

  return <Link to={electionUrl}><FormattedMessage {...NicheProfileMessages.ViewElection}/></Link>;
}

const NicheModeratorsSectionComponent: React.SFC<WithNicheModeratorSlotsProps> = (props) => {
  const { nicheModeratorSlotsLoading } = props;

  if (nicheModeratorSlotsLoading) {
    return (
      <ChannelDetailsSection title={<FormattedMessage {...NicheProfileMessages.Moderators}/>}>
        <ContainedLoading />
      </ChannelDetailsSection>
    );
  }

  const { moderatorSlots } = props;

  const headerExtra = getViewActiveElectionLink(props);

  const placeholdersPerColumn = Math.floor(moderatorSlots / 3);
  const remainingPlaceholders = moderatorSlots % 3;

  return (
    <ChannelDetailsSection
      title={
        <FormattedMessage
          {...NicheProfileMessages.ModeratorsWithSlots}
          values={{moderatorSlots}}
        />
      }
      extra={headerExtra}
    >
      <Row gutter={15}>
      {[1, 2, 3].map((columnNumber) => {
        // if we have less results the number of this column, there is no need to include it.
        if (columnNumber > moderatorSlots) {
          return null;
        }

        // jw: if the number of this column is not greater than the number of remainders we have, then include an extra
        //     placeholder to ensure that all are represented.
        const extraPlaceholders = columnNumber <= remainingPlaceholders ? 1 : 0;

        return (
          <Col key={columnNumber} md={8}>
            {generateOpenPositionPlaceholders(placeholdersPerColumn + extraPlaceholders)}
          </Col>
        );
      })}
      </Row>
    </ChannelDetailsSection>
  );
};

export const NicheModeratorsSection = compose(
  withProps((props: WithNicheDetailsContextProps) => {
    const { nicheDetail: { niche } } = props;
    return { nicheId: niche.oid};
  }),
  withNicheModeratorSlots
)(NicheModeratorsSectionComponent) as React.ComponentClass<WithNicheDetailsContextProps>;

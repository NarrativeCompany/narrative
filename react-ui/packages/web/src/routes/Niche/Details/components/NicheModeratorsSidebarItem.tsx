import * as React from 'react';
import { compose, withProps } from 'recompose';
import { withNicheModeratorSlots, WithNicheModeratorSlotsProps } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { SidebarMessages } from '../../../../shared/i18n/SidebarMessages';
import { SectionHeader } from '../../../../shared/components/SectionHeader';
import { ContainedLoading } from '../../../../shared/components/Loading';
import { WithNicheDetailsContextProps } from './NicheDetailsContext';
import { generateOpenPositionPlaceholders }
  from '../../../../shared/components/moderatorElection/OpenModeratorPositionPlaceholder';
import { Card } from '../../../../shared/components/Card';
import { CardSpacingContainer } from '../../../../shared/components/sidebar/TrendingNichesSidebarItem';
import { getViewActiveElectionLink } from '../Profile/components/NicheModeratorsSection';
import styled from '../../../../shared/styled';

const ModeratorSlotsWrapper = styled.div`

  .ant-card:last-child {
    margin-bottom:0;
  }
`;

const NicheModeratorsSidebarComponent: React.SFC<WithNicheModeratorSlotsProps> = (props) => {
  const { nicheModeratorSlotsLoading } = props;
  const { moderatorSlots } = props;

  if (nicheModeratorSlotsLoading) {
    return (
      <SectionHeader
        title={<FormattedMessage {...SidebarMessages.NicheModeratorsHeaderWithoutValues}/>}
      >
        <CardSpacingContainer>
          <Card>
            <ContainedLoading />
          </Card>
        </CardSpacingContainer>
      </SectionHeader>
    );
  }

  const headerExtra = getViewActiveElectionLink(props);
  const slotsToDisplay = Math.min(moderatorSlots, 10);

  return (
    <React.Fragment>
      <SectionHeader
        title={
          <FormattedMessage
            {...SidebarMessages.NicheModeratorsHeaderWithValues}
            values={{moderatorSlots}}
          />
        }
        extra={headerExtra}
      />
      <CardSpacingContainer>
        <Card>
          <ModeratorSlotsWrapper>
            {generateOpenPositionPlaceholders(slotsToDisplay)}
          </ModeratorSlotsWrapper>
        </Card>
      </CardSpacingContainer>
    </React.Fragment>
  );
};

export const NicheModeratorsSidebarItem = compose(
  withProps((props: WithNicheDetailsContextProps) => {
    const { nicheDetail: { niche } } = props;
    return { nicheId: niche.oid};
  }),
  withNicheModeratorSlots
)(NicheModeratorsSidebarComponent) as React.ComponentClass<WithNicheDetailsContextProps>;

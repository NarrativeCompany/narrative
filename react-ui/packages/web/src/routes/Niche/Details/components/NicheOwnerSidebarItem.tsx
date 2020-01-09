import * as React from 'react';
import { SectionHeader } from '../../../../shared/components/SectionHeader';
import { FormattedMessage } from 'react-intl';
import { SidebarMessages } from '../../../../shared/i18n/SidebarMessages';
import { WithNicheDetailsContextProps } from './NicheDetailsContext';
import { MemberAvatar } from '../../../../shared/components/user/MemberAvatar';
import { MemberLink } from '../../../../shared/components/user/MemberLink';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { MemberReputationBadge } from '../../../../shared/components/user/MemberReputationBadge';
import { Card } from '../../../../shared/components/Card';
import { CardSpacingContainer } from '../../../../shared/components/sidebar/TrendingNichesSidebarItem';

export const NicheOwnerSidebarItem: React.SFC<WithNicheDetailsContextProps> = (props) => {

  const { nicheDetail: { niche: { owner } } } = props;

  if (!owner) {
    // zb: todo:error-handling: We should always have a owner at this point!
    return null;
  }

  return (
    <React.Fragment>
      <SectionHeader
        title={<FormattedMessage {...SidebarMessages.NicheOwnerHeader} />}
      />
      <CardSpacingContainer>
        <Card>
          <FlexContainer alignItems="center" justifyContent="flex-start">
            <MemberAvatar size={25} style={{marginRight: 10}} user={owner} />

            <div>
              <MemberLink user={owner} color="dark" size="large" hideBadge={true} weight={400}/>
            </div>
            <FlexContainer alignItems="center" style={{flexGrow: 1}} justifyContent="flex-end">
              <MemberReputationBadge
                badgeSize="large"
                user={owner}
              />
            </FlexContainer>
          </FlexContainer>
        </Card>
      </CardSpacingContainer>
    </React.Fragment>
  );
};

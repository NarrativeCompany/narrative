import * as React from 'react';
import { FormattedMessage } from 'react-intl';
import { NicheProfileMessages } from '../../../../../shared/i18n/NicheProfileMessages';
import styled from '../../../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../../../shared/styled/shared/containers';
import { WithNicheDetailsContextProps } from '../../components/NicheDetailsContext';
import { MemberAvatar } from '../../../../../shared/components/user/MemberAvatar';
import { MemberLink } from '../../../../../shared/components/user/MemberLink';
import { ChannelDetailsSection } from '../../../../../shared/components/channel/ChannelDetailsSection';

const OwnerWrapper = styled<FlexContainerProps>(FlexContainer)`
  max-width: 175px;
`;

export const NicheOwnerSection: React.SFC<WithNicheDetailsContextProps> = (props) => {
  const { nicheDetail: { niche: { owner } } } = props;

  if (!owner) {
    // jw: todo:error-handling: We should always have a owner at this point!
    return null;
  }

  return (
    <ChannelDetailsSection title={<FormattedMessage {...NicheProfileMessages.NicheOwnerSectionTitle}/>}>
      <OwnerWrapper column={true} alignItems="center" justifyContent="flex-start">
        <MemberAvatar size={175} user={owner} style={{marginBottom: 15}}/>

        <MemberLink user={owner} color="dark" size="large"  weight={400}/>
      </OwnerWrapper>
    </ChannelDetailsSection>
  );
};

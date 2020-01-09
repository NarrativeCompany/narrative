import * as React from 'react';
import { User } from '@narrative/shared';
import styled from '../../../../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../../../../shared/styled/shared/containers';
import { MemberLink } from '../../../../../shared/components/user/MemberLink';
import { Text } from '../../../../../shared/components/Text';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { MemberAvatar } from '../../../../../shared/components/user/MemberAvatar';

interface Props {
  user: User;
  pending?: boolean;
  tool?: React.ReactNode;
}

const distanceBetweenInlineELements = 10;

const Wrapper = styled<FlexContainerProps>(FlexContainer)`
  padding: 10px 15px;
  margin-bottom: 15px;
  &:hover {
    background-color: ${p => p.theme.defaultTagBackgroundColorHover};
    border-radius: 10px;
  }
`;

export const PowerUserRow: React.SFC<Props> = (props) => {
  const { user, pending, tool } = props;

  return (
    <Wrapper alignItems="center">
      <MemberAvatar user={user} />
      <MemberLink user={user} hideBadge={true} style={{marginLeft: distanceBetweenInlineELements}} />
      {pending &&
        <Text color="warning" style={{marginLeft: distanceBetweenInlineELements}}>
          <FormattedMessage {...PublicationDetailsMessages.PowerUserPendingLabel}/>
        </Text>
      }
      {tool &&
        // jw: We want the tool to the on the far right, so let's give this a auto margin left which will push it over.
        <Text style={{marginLeft: 'auto'}}>{tool}</Text>
      }
    </Wrapper>
  );
};

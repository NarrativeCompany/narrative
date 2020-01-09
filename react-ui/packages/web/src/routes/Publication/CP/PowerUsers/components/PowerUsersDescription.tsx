import * as React from 'react';
import { Block } from '../../../../../shared/components/Block';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { distanceBetweenSections } from '../../../../../shared/components/Section';
import styled from '../../../../../shared/styled';
import { FormattedMessage } from 'react-intl';
import { Col, Row } from 'antd';
import { InvitePowerUserButton, InvitePowerUserButtonProps } from './InvitePowerUserButton';
import { ColProps } from 'antd/lib/grid';
import { mediaQuery } from '../../../../../shared/styled/utils/mediaQuery';

const ButtonColumn = styled<ColProps>(Col)`
  text-align: right;
  
  ${mediaQuery.md_down`
    text-align: left;
    margin-top: 15px;
  `}
`;

// jw: note: this is consuming the button props because that is the only reason it is taking any props, so this makes
//     it the most clear what they are for.
export const PowerUsersDescription: React.SFC<InvitePowerUserButtonProps> = (props) => {
  const { canInviteRoles, currentUserRoles } = props;

  // jw: we want to include the button if the current user can invite any roles, or they are an admin/editor, since
  //     that means that we have reached the limit for the roles they can invite and we need to tell them that.
  const includeButton = canInviteRoles.length > 0 || currentUserRoles.admin || currentUserRoles.editor;

  // jw: if we are not including the button then just short out with the description.
  if (!includeButton) {
    return (
      <Block style={{marginBottom: distanceBetweenSections}}>
        <FormattedMessage {...PublicationDetailsMessages.PowerUsersDescription} />
      </Block>
    );
  }

  return (
    <Block style={{marginBottom: distanceBetweenSections}}>
      <Row gutter={24}>
        <Col lg={16}>
          <FormattedMessage {...PublicationDetailsMessages.PowerUsersDescription} />
        </Col>
        <ButtonColumn lg={8}>
          <InvitePowerUserButton {...props} />
        </ButtonColumn>
      </Row>
    </Block>
  );
};

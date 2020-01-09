import * as React from 'react';
import { compose } from 'recompose';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { WebRoute } from '../../../shared/constants/routes';
import { HeaderNavIcon } from './HeaderNavIcon';
import { RevokeReasonMessages } from '../../../shared/i18n/RevokeReasonMessages';
import styled from '../../../shared/styled';
import { PermissionErrorModal } from '../../../shared/components/PermissionErrorModal';
import {
  withPermissionsModalHelpers,
  WithPermissionsModalHelpersProps
} from '../../../shared/containers/withPermissionsModalHelpers';
import { externalUrls } from '../../../shared/constants/externalUrls';

const NavLinkWrapper = styled.span`
  margin-right: 30px;

  &:last-child {
    margin-right: 10px;
  }
`;

const HeaderRightNavComponent: React.SFC<WithPermissionsModalHelpersProps> = (props) => {
  const { permissionErrorModalProps, permissionLinkSecurer } = props;

  return (
    <React.Fragment>
      <FlexContainer alignItems="center">
        <NavLinkWrapper>
          <HeaderNavIcon
            icon="search"
            to={WebRoute.Search}
          />
        </NavLinkWrapper>

        <NavLinkWrapper>
          <HeaderNavIcon
            icon="info-circle"
            href={externalUrls.narrativeAboutWebsite}
          />
        </NavLinkWrapper>

        <NavLinkWrapper>
          <HeaderNavIcon
            icon="form"
            to={WebRoute.Post}
            linkSecurer={permissionLinkSecurer}
          />
        </NavLinkWrapper>
      </FlexContainer>
      {permissionErrorModalProps && <PermissionErrorModal {...permissionErrorModalProps} />}
    </React.Fragment>

  );
};

export const HeaderRightNav = compose(
  withPermissionsModalHelpers('postContent', RevokeReasonMessages.PostContent)
)(HeaderRightNavComponent) as React.ComponentClass<{}>;

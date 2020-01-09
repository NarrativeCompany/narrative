import * as React from 'react';
import styled from '../../../shared/styled';
import { Publication } from '@narrative/shared';
import { Link, LinkProps } from '../../../shared/components/Link';
import { WebRoute } from '../../../shared/constants/routes';
import { Icon } from 'antd';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { getChannelUrl } from '../../../shared/utils/channelUtils';
import { FollowChannelButton } from '../../../shared/components/FollowChannelButton';
import { publicationIconStyle, PublicationSocialLinks } from './PublicationSocialLinks';

interface Props {
  publication: Publication;
  tabBar: React.ReactNode;
  socialLinks?: React.ReactNode[];
}

const BarContainer = styled.div`
  position: relative;
`;

const RightTools = styled<FlexContainerProps>(FlexContainer)`
  position: absolute;
  right: 0;
  bottom: 25px;
  > *:not(:first-child) {
    margin-left: 10px;
  }
`;

const SearchIconLink = styled<LinkProps>(Link)`
  ${publicationIconStyle}
`;

export const PublicationTabBar: React.SFC<Props> = (props) => {
  const { publication, tabBar, socialLinks } = props;

  return (
    <BarContainer>
      {tabBar}

      <RightTools alignItems="center">
        <SearchIconLink to={getChannelUrl(publication, WebRoute.PublicationSearch)}>
          <Icon type="search" />
        </SearchIconLink>

        <PublicationSocialLinks links={socialLinks} forHeader={true} />

        <FollowChannelButton channel={publication} />
      </RightTools>
    </BarContainer>
  );
};

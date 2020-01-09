import * as React from 'react';
import { NicheDetail, Publication } from '@narrative/shared';
import { PublicationTopNichesHeader } from './PublicationTopNichesHeader';
import { PublicationLink } from '../../../../../shared/components/publication/PublicationLink';
import { Icon } from '../../../../../shared/components/Icon';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { FormattedMessage } from 'react-intl';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { Heading } from '../../../../../shared/components/Heading';
import { NicheLink } from '../../../../../shared/components/niche/NicheLink';
import { themeColors } from '../../../../../shared/styled/theme';
import { IconProps } from 'antd/lib/icon';
import styled from 'styled-components';
import { mediaQuery } from '../../../../../shared/styled/utils/mediaQuery';

interface Props {
  publication: Publication;
  nicheDetail?: NicheDetail;
}

const NicheLinkIcon = styled<IconProps>(Icon)`
  margin-left: 10px;
  color: ${themeColors.primaryBlue};
  font-size: 20px;
  vertical-align: baseline;
`;

const TopNichesContainer = styled.div`
  margin-bottom: 15px;
  // jw: paralleling SidebarViewWrapper.SidebarCol which hides on md_down
  ${mediaQuery.hide_md_down}
`;

export const PublicationPostsNicheHeader: React.SFC<Props> = (props) => {
  const { publication, nicheDetail } = props;

  if (!nicheDetail) {
    return (
      <TopNichesContainer>
        <PublicationTopNichesHeader publication={publication} />
      </TopNichesContainer>
    );
  }

  return (
    <React.Fragment>
      <Paragraph marginBottom="small">
        <Icon type="left-circle" />
        {' '}
        <PublicationLink publication={publication}>
          <FormattedMessage {...PublicationDetailsMessages.AllPosts}/>
        </PublicationLink>
      </Paragraph>
      <Heading size={2}>
        {nicheDetail.niche.name}
        <NicheLink niche={nicheDetail.niche}>
          <NicheLinkIcon type="select" />
        </NicheLink>
      </Heading>
    </React.Fragment>
  );
};

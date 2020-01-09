import * as React from 'react';
import { Layout } from 'antd';
import { LayoutProps } from 'antd/lib/layout';
import { FooterContent } from './FooterContent';
import styled from '../../shared/styled';
import { FlexContainer, FlexContainerProps } from '../../shared/styled/shared/containers';
import { SharedComponentMessages } from '../../shared/i18n/SharedComponentMessages';
import { FormattedMessage } from 'react-intl';
import { mediaQuery } from '../../shared/styled/utils/mediaQuery';

type Props =
  LayoutProps;

const BetaWrapper = styled<FlexContainerProps>(FlexContainer)`
  justify-content: center;
  margin-bottom: 10px;
  color: ${props => props.theme.textColorLight};
`;

const FooterWrapper = styled(Layout.Footer)`
  padding: 0;
  
  ${mediaQuery.md_down`
    margin-bottom: 68px;
  `};
`;

export const Footer: React.SFC<Props> = (props) => (
  <React.Fragment>
    <BetaWrapper>
      <FormattedMessage {...SharedComponentMessages.NarrativeInBeta}/>
    </BetaWrapper>
    <FooterWrapper {...props}>
      <FooterContent/>
    </FooterWrapper>
  </React.Fragment>
);

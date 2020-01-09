import * as React from 'react';
import { Alert, Divider, Layout } from 'antd';
import { LayoutProps } from 'antd/lib/layout';
import { Logo } from '../../shared/components/Logo';
import { FlexContainer, FlexContainerProps } from '../../shared/styled/shared/containers';
import styled from '../../shared/styled';
import { mediaQuery } from '../../shared/styled/utils/mediaQuery';
import { defaultViewWrapperPadding, getViewportMaxWidth, ViewWrapperProps } from '../../shared/components/ViewWrapper';
import { HeaderBg } from '../../shared/stores/LayoutBgStore';
import { compose } from 'recompose';
import { withShutdownNoticeUrl, WithShutdownNoticeUrlProps } from '@narrative/shared';
import { SharedComponentMessages } from '../../shared/i18n/SharedComponentMessages';
import { FormattedMessage } from 'react-intl';
import { Link } from '../../shared/components/Link';

// tslint:disable-next-line no-any
const HeaderWrapper = styled<
  LayoutProps & {position?: HeaderPosition, bgColor: HeaderBg, showHeaderNavOnHover?: boolean}
>(
  ({position, bgColor, showHeaderNavOnHover, ...rest}) => <Layout.Header {...rest} />
)`
  position: relative;
  z-index: 10;
  padding: 0;
  display: flex;
  align-items: center;
  line-height: initial;
  height: 50px;
  transition: all .15s ease-in-out;
  background: ${p => p.theme.headerBackground[p.bgColor]};
  box-shadow: 0 2px 10px 0 rgba(0,0,0,0.05);
  
 
  ${p => p.position === 'fixed' && `
    position: fixed;
    left: 0;
    right: 0;
  `}
  
  ${mediaQuery.md_up`
    height: 64px;
    transition: all .15s ease-in-out;
  `}
  
  ${p => p.showHeaderNavOnHover && mediaQuery.lg_up`
    &:hover > div > div > *:not(.narrative-logo) {
      display: inherit;
    }
  `}
`;

const HeaderInner = styled<FlexContainerProps>(FlexContainer)`
  max-width: ${props => props.theme.layoutMaxWidth + (2 * defaultViewWrapperPadding)}px;
  width: 100%;
  margin: 0 auto;
  padding: 0 ${defaultViewWrapperPadding}px;
  
  @media screen and (max-width: 576px) {
    padding: 0 ${defaultViewWrapperPadding / 2}px;
  }
`;

const HeaderLeftWrapper = styled<FlexContainerProps & {showHeaderNavOnHover?: boolean}>(FlexContainer)`
  margin-right: auto;
  
  .ant-divider {
    height: 25px;
    margin: 0 20px;
  }
  
  ${mediaQuery.md_down`
    .ant-divider {
      display: none;
    }
  `}
  
  ${p => p.showHeaderNavOnHover && mediaQuery.lg_up`
    > *:not(.narrative-logo) {
      display: none;
    }
  `}
`;

type HeaderPosition = 'static' | 'fixed';

const ShutdownNoticeWrapper = styled<ViewWrapperProps>(FlexContainer)`
  width: 100%;
  max-width: ${p => getViewportMaxWidth(p)}px;
  margin: 0 auto;
  padding: 24px 24px 0;
`;

type ParentProps =
  LayoutProps & {
  headerLeftContent?: React.ReactNode;
  headerRightContent?: React.ReactNode;
  showHeaderNavOnHover?: boolean;
  position?: HeaderPosition;
  bgColor?: HeaderBg;
  logoIsLink?: boolean;
};

type Props =
  ParentProps &
  WithShutdownNoticeUrlProps;

const HeaderComponent: React.SFC<Props> = (props) => {
  const {
    headerLeftContent,
    headerRightContent,
    showHeaderNavOnHover,
    position,
    logoIsLink,
    bgColor,
    shutdownNoticeUrl
  } = props;

  const shutdownNoticeLink = shutdownNoticeUrl && (
    <Link.Anchor href={shutdownNoticeUrl}>
      <FormattedMessage {...SharedComponentMessages.ReadMoreHere}/>
    </Link.Anchor>
  );

  return (
    <React.Fragment>
      <HeaderWrapper position={position} bgColor={bgColor || 'white'} showHeaderNavOnHover={showHeaderNavOnHover}>
        <HeaderInner>
          <HeaderLeftWrapper
            alignItems="center"
            justifyContent="flex-start"
            showHeaderNavOnHover={showHeaderNavOnHover}>
            <Logo isLink={logoIsLink !== undefined ? logoIsLink : true}/>
            <Divider type="vertical"/>

            {headerLeftContent}
          </HeaderLeftWrapper>

          {headerRightContent}
        </HeaderInner>
      </HeaderWrapper>
      {shutdownNoticeLink &&
        <ShutdownNoticeWrapper>
          <Alert
            type="warning"
            message={<FormattedMessage {...SharedComponentMessages.NarrativeEndOfLife}/>}
            description={<FormattedMessage
              {...SharedComponentMessages.NarrativeEndOfLifeDescription}
              values={{shutdownNoticeLink}}
            />}
            showIcon={true}
            closable={true}
            style={{width: '100%'}}
          />
        </ShutdownNoticeWrapper>
      }
    </React.Fragment>
  );
};

export const Header = compose(
  withShutdownNoticeUrl
)(HeaderComponent) as React.ComponentClass<ParentProps>;

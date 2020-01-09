import * as React from 'react';
import { compose } from 'recompose';
import { Layout as AntLayout } from 'antd';
import { LayoutProps } from 'antd/lib/layout';
import { Header } from './Header/Header';
import { Content } from './Content/Content';
import { Footer } from './Footer/Footer';
import { ScrollToTop } from '../shared/components/ScrollToTop';
import { ErrorBoundary } from '../shared/components/ErrorBoundary';
import { NotFound } from '../shared/components/NotFound';
import { HeaderNav } from './Header/components/HeaderNav';
import { HeaderRight } from './Header/components/HeaderRight';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { SharedComponentMessages } from '../shared/i18n/SharedComponentMessages';
import { LayoutBg, LayoutBgConnect, LayoutBgStoreProps } from '../shared/stores/LayoutBgStore';
import styled from '../shared/styled';
import { RouteComponentProps, withRouter } from 'react-router';

const LayoutMain =
  styled<LayoutProps & {layoutBg: LayoutBg}>(({layoutBg, ...rest}) => <AntLayout {...rest}/>)`
    background: ${p => p.theme.layoutBackground[p.layoutBg]};  
    min-height: 100%;
  `;

interface ParentProps extends LayoutProps {
  children: React.ReactNode;
}

type Props =
  ParentProps &
  InjectedIntlProps &
  LayoutBgStoreProps &
  RouteComponentProps<{}>;

const LayoutComponent: React.SFC<Props> = (props) => {
  const { layoutBg, headerBg, showHeaderNavOnHover, children, intl: { formatMessage } } = props;
  const errorMessage = <FormattedMessage {...SharedComponentMessages.DefaultErrorMessage}/>;
  const errorTitle = formatMessage(SharedComponentMessages.DefaultErrorTitle);

  return (
    <ScrollToTop>
      <LayoutMain layoutBg={layoutBg}>
        <Header
          bgColor={headerBg}
          headerLeftContent={<HeaderNav/>}
          headerRightContent={<HeaderRight/>}
          showHeaderNavOnHover={showHeaderNavOnHover}
        />

        <ErrorBoundary generateError={() => <NotFound title={errorTitle} message={errorMessage} statusCode={500}/>}>
          <Content>
            {children}
          </Content>
        </ErrorBoundary>

        <Footer/>

        <HeaderNav isMobile={true}/>
      </LayoutMain>
    </ScrollToTop>
  );
};

export const Layout = compose(
  injectIntl,
  LayoutBgConnect,
  withRouter
)(LayoutComponent) as React.ComponentClass<ParentProps>;

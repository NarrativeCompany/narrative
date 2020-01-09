import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../components/PublicationDetailsContext';
import { TabDetails, TabRoute } from '../../../shared/containers/withTabsController';
import { WebRoute } from '../../../shared/constants/routes';
import { PublicationDetailsMessages } from '../../../shared/i18n/PublicationDetailsMessages';
import * as Routes from '../../index';
import { getIdForUrl } from '../../../shared/utils/routeUtils';
import {
  withVerticalMenuController,
  WithVerticalMenuControllerProps
} from '../../../shared/containers/withVerticalMenuController';
import { Col, Row } from 'antd';
import styled from '../../../shared/styled';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { ColProps } from 'antd/lib/grid';
import { generatePath, Redirect } from 'react-router';

const MenuCol = styled<ColProps>(Col)`
  ${mediaQuery.hide_sm_down};
`;

const PublicationCPComponent: React.SFC<WithVerticalMenuControllerProps> = (props) => {
  const { verticalMenu, narrowViewportMenu, selectedTabRoute } = props;

  return (
    <Row gutter={16}>
      <MenuCol md={4}>
        {verticalMenu}
      </MenuCol>
      <Col md={20}>
        {narrowViewportMenu}

        {selectedTabRoute}
      </Col>
    </Row>
  );
};

export default compose(
  PublicationDetailsConnect,
  withProps((props: WithPublicationDetailsContextProps) => {
    const { currentUserRoles, publicationDetail: { publication } } = props;
    const  { oid, prettyUrlString } = publication;

    const tabs: TabDetails[] = [];

    if (currentUserRoles.admin) {
      tabs.push(new TabDetails(
        new TabRoute(WebRoute.PublicationSettings),
        PublicationDetailsMessages.Settings,
        Routes.PublicationSettings
      ));
    }

    // jw: if the user has any specific role then they can/should be able to access the power users control panel. Even
    //     if it is just to be able to remove themselves.
    if (currentUserRoles.owner || currentUserRoles.admin || currentUserRoles.editor || currentUserRoles.writer) {
      tabs.push(new TabDetails(
        new TabRoute(WebRoute.PublicationPowerUsers),
        PublicationDetailsMessages.PowerUsers,
        Routes.PublicationPowerUsers
      ));
    }

    if (currentUserRoles.editor) {
      tabs.push(new TabDetails(
        new TabRoute(WebRoute.PublicationReviewQueue),
        PublicationDetailsMessages.ReviewQueue,
        Routes.PublicationReviewQueue
      ));
    }

    if (currentUserRoles.owner) {
      tabs.push(new TabDetails(
        new TabRoute(WebRoute.PublicationAccount),
        PublicationDetailsMessages.Account,
        Routes.PublicationAccount
      ));
    }

    // jw: we will need this id for the tabs controller and for the default route match below.
    const id = getIdForUrl(prettyUrlString, oid);

    // jw: to ensure that any requests to /manage are redirected to the first page the user has access to we need to add
    //     a hidden tab that will catch that and do the redirect.
    const firstTab = tabs[0];
    const redirectTab = new TabDetails(
        new TabRoute(WebRoute.PublicationCP, true),
        PublicationDetailsMessages.Manage,
        () => <Redirect to={generatePath(firstTab.path || '', {id})} />
      );
    redirectTab.hidden = true;
    tabs.push(redirectTab);

    return {
      tabs,
      tabRouteParams: { id }
    };
  }),
  withVerticalMenuController
)(PublicationCPComponent) as React.ComponentClass<{}>;

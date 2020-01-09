import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  WithPublicationDetailsContextProps
} from '../../components/PublicationDetailsContext';
import { TabDetails, TabRoute } from '../../../../shared/containers/withTabsController';
import { WebRoute } from '../../../../shared/constants/routes';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import * as Routes from '../../../index';
import { getIdForUrl } from '../../../../shared/utils/routeUtils';
import {
  withPillMenuController,
  WithPillMenuControllerProps
} from '../../../../shared/containers/withPillMenuController';
import { PillMenu } from '../../../../shared/components/navigation/pills/PillMenu';
import { withExpiredPublicationError } from '../../components/withExpiredPublicationError';

const PublicationAboutComponent: React.SFC<WithPillMenuControllerProps> = (props) => {
  const { pillMenuProps, selectedTabRoute } = props;

  return (
    <React.Fragment>
      <PillMenu {...pillMenuProps} />

      {selectedTabRoute}
    </React.Fragment>
  );
};

export default compose(
  withExpiredPublicationError(),
  withProps((props: WithPublicationDetailsContextProps) => {
    const tabs: TabDetails[] = [];

    // jw: The info page is the default page for the about route
    tabs.push(new TabDetails(
      new TabRoute(WebRoute.PublicationAbout),
      PublicationDetailsMessages.InfoPillText,
      Routes.PublicationInfo
    ));

    tabs.push(new TabDetails(
      new TabRoute(WebRoute.PublicationActivity),
      PublicationDetailsMessages.ActivityPillText,
      Routes.PublicationActivity
    ));

    const { publicationDetail: { publication: { prettyUrlString, oid } } } = props;
    const id = getIdForUrl(prettyUrlString, oid);

    return {
      tabs,
      tabRouteParams: { id }
    };
  }),
  withPillMenuController
)(PublicationAboutComponent) as React.ComponentClass<{}>;

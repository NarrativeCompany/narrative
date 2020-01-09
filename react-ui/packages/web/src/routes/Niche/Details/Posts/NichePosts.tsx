import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { ContentStreamChannelType, withChannelContentStream } from '@narrative/shared';
import {
  withContentStreamSorts,
  WithContentStreamSortsProps
} from '../../../../shared/containers/withContentStreamSorts';
import { WebRoute } from '../../../../shared/constants/routes';
import {
  ContentStream,
  WithContentStreamPropsFromQuery,
  withContentStreamPropsFromQuery
} from '../../../../shared/components/contentStream/ContentStream';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { SEO } from '../../../../shared/components/SEO';
import { NicheDetailsMessages } from '../../../../shared/i18n/NicheDetailsMessages';
import { NicheDetailsConnect, WithNicheDetailsContextProps } from '../components/NicheDetailsContext';
import { NotFound } from '../../../../shared/components/NotFound';
import { EnhancedNicheStatus } from '../../../../shared/enhancedEnums/nicheStatus';
import { PillMenu } from '../../../../shared/components/navigation/pills/PillMenu';
import { getIdForUrl } from '../../../../shared/utils/routeUtils';

type Props =
  WithNicheDetailsContextProps &
  WithContentStreamPropsFromQuery &
  WithContentStreamSortsProps &
  InjectedIntlProps;

const NichePostsComponent: React.SFC<Props> = (props) => {
  const {
    nicheDetail,
    intl: {formatMessage},
    contentStreamProps,
    pillMenuProps
  } = props;
  const { niche } = nicheDetail;
  const nicheName = niche.name;

  return (
    <React.Fragment>
      <SEO
        title={formatMessage(NicheDetailsMessages.PostsSeoTitle, {nicheName})}
        description={niche.description}
      />

      <PillMenu {...pillMenuProps} />

      <ContentStream {...contentStreamProps}/>
    </React.Fragment>
  );
};

export default compose(
  NicheDetailsConnect,
  // jw: technically, this should never happen since the routing in NicheDetails provided by withCardTabsController will
  //     do exactly what we are doing here. Still, I'm paranoid, so let's be cautious.
  branch((props: WithNicheDetailsContextProps) => (!EnhancedNicheStatus.get(props.nicheDetail.niche.status).isActive()),
    renderComponent(() => <NotFound/>)
  ),
  withProps((props: WithNicheDetailsContextProps) => {
    const { nicheDetail: { niche } } = props;
    const { oid, prettyUrlString } = niche;

    const id = getIdForUrl(prettyUrlString, oid);

    return {
      baseParameters: { id },
      channelType: ContentStreamChannelType.niches,
      channelOid: niche.oid
    };
  }),
  withContentStreamSorts(WebRoute.NicheDetails, WebRoute.NichePosts),
  withChannelContentStream,
  // jw: now, let's make those search results easy to consume.
  withContentStreamPropsFromQuery(),
  injectIntl
)(NichePostsComponent) as React.ComponentClass<{}>;

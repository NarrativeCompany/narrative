import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import {
  ContentStreamChannelType,
  withChannelContentStream,
  ContentStreamSortOrder,
  Post,
  getQueryArg, withNicheDetail, WithNicheDetailProps
} from '@narrative/shared';
import {
  ContentStream,
  WithContentStreamPropsFromQuery,
  withContentStreamPropsFromQuery
} from '../../../../shared/components/contentStream/ContentStream';
import { SEO } from '../../../../shared/components/SEO';
import { getIdForUrl } from '../../../../shared/utils/routeUtils';
import {
  WithPublicationDetailsContextProps
} from '../../components/PublicationDetailsContext';
import { PublicationDetailsMessages } from '../../../../shared/i18n/PublicationDetailsMessages';
import { PublicationTopNichesSidebarItem } from './components/PublicationTopNichesSidebarItem';
import { RouteComponentProps, withRouter } from 'react-router';
import { PublicationTrendingPostsSidebarItem } from './components/PublicationTrendingPostsSidebarItem';
import { PublicationDetailSidebarItem } from './components/PublicationDetailsSidebarItem';
import { PublicationSidebarViewWrapper } from '../../components/PublicationSidebarViewWrapper';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { NotFound } from '../../../../shared/components/NotFound';
import { PublicationPostsNicheHeader } from './components/PublicationPostsNicheHeader';
import { nicheParam } from '../../../../shared/utils/publicationUtils';
import { withExpiredPublicationError } from '../../components/withExpiredPublicationError';

type Props =
  WithPublicationDetailsContextProps &
  WithContentStreamPropsFromQuery &
  // jw: note, while these props define the nicheDetail as being always provided they will only actually be set if we
  //     have a `nicheParam` query arg when rendering.
  WithNicheDetailProps;

const featuredSplitPointCalculator = (posts: Post[]): number => {
  // jw: this is pretty simple, the split point is the first post that is not featured since all featured posts should
  //     be first.
  const index = posts.findIndex((post) => !post.featuredInPublication);

  // jw: if all of the posts are featured then the cutoff point is the very end of the list.
  return index >= 0 ? index : posts.length;
};

const PublicationPostsComponent: React.SFC<Props> = (props) => {
  const { publicationDetail: { publication }, nicheDetail, contentStreamProps } = props;

  if (!contentStreamProps.sortOrder) {
    // todo:error-handling: This should never happen since the withContentStreamSorts will establish a sort order
    return null;
  }

  return (
    <React.Fragment>
      <SEO
        title={PublicationDetailsMessages.PublicationPostsSeoTitle}
        description={publication.description}
        publication={publication}
      />

      <PublicationSidebarViewWrapper
        sidebarItems={
          <React.Fragment>
            <PublicationDetailSidebarItem publication={publication} />
            <PublicationTopNichesSidebarItem publication={publication} />
            <PublicationTrendingPostsSidebarItem publication={publication} />
          </React.Fragment>
        }
      >
        <PublicationPostsNicheHeader
          publication={publication}
          nicheDetail={nicheDetail}
        />
        <ContentStream
          {...contentStreamProps}
          featuredSplitPointCalculator={featuredSplitPointCalculator}
          forPublicationDisplay={true}
        />
      </PublicationSidebarViewWrapper>
    </React.Fragment>
  );
};

interface WithProps {
  nicheId?: string;
}

export default compose(
  withExpiredPublicationError(),
  withRouter,
  withProps((props: WithPublicationDetailsContextProps & RouteComponentProps<{}>) => {
    const { publicationDetail: { publication }, location: { search } } = props;
    const { oid, prettyUrlString } = publication;

    const id = getIdForUrl(prettyUrlString, oid);
    const nicheOid = getQueryArg(search, nicheParam);

    return {
      baseParameters: { id },
      channelType: ContentStreamChannelType.publications,
      channelOid: oid,
      sortOrder: ContentStreamSortOrder.MOST_RECENT,
      nicheOid,
      // jw: let's include the nicheId property which is consumed by withNicheDetail
      nicheId: nicheOid
    };
  }),
  withChannelContentStream,
  // jw: now that the posts are loading, let's load the niche details and block loading until it is done. The
  //     withContentStreamPropsFromQuery HOC does a great job allowing the UI to render while loading, so no need to
  //     hold things up beyond the niche details.
  // jw: note: we only want to try to load the niche details if we have a nicheId to work with.
  branch<WithProps>(props => !!props.nicheId,
    compose(
      withNicheDetail,
      withLoadingPlaceholder(fullPlaceholder),
      // jw: if we were given a nicheOid and it does not map to a niche, then we need to give a not found response
      branch<WithNicheDetailProps>(props => !props.nicheDetail,
        renderComponent(() => <NotFound/>)
      )
    )
  ),
  // jw: now, let's make those search results easy to consume.
  withContentStreamPropsFromQuery(PublicationDetailsMessages.NoPublicationContentStreamResults)
)(PublicationPostsComponent) as React.ComponentClass<{}>;

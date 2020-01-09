import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  WithPaginationControllerProps,
  withQueryParamPaginationController
} from '../../../../../shared/containers/withPaginationController';
import { createUrl, getIdForUrl } from '../../../../../shared/utils/routeUtils';
import { generatePath } from 'react-router';
import { WebRoute } from '../../../../../shared/constants/routes';
import {
  Publication,
  withModeratedPublicationPosts, WithModeratedPublicationPostsParentProps,
  WithModeratedPublicationPostsProps
} from '@narrative/shared';
import { fullPlaceholder, withLoadingPlaceholder } from '../../../../../shared/utils/withLoadingPlaceholder';
import { ReviewQueueModeratedPosts } from './ReviewQueueModeratedPosts';
import { Pagination } from 'antd';
import styled from '../../../../../shared/styled';
import { FlexContainer } from '../../../../../shared/styled/shared/containers';
import { Heading } from '../../../../../shared/components/Heading';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { LocalizedNumber } from '../../../../../shared/components/LocalizedNumber';

const pageParam = 'page';

const PaginationWrapper = styled(FlexContainer)`
  margin-top: 20px;
`;

interface ParentProps {
  publication: Publication;
}

type Props = ParentProps &
  WithModeratedPublicationPostsProps &
  WithPaginationControllerProps;

const ReviewQueueBodyComponent: React.SFC<Props> = (props) => {
  const { moderatedPosts, pagination, pageInfo } = props;

  return (
    <React.Fragment>
      <Heading size={2}>
        <FormattedMessage
          {...PublicationDetailsMessages.ReviewQueueTitle}
          values={{postCount: <LocalizedNumber value={pageInfo.totalElements}/>}}
        />
      </Heading>
      <Paragraph marginBottom="large">
        <FormattedMessage {...PublicationDetailsMessages.ReviewQueueDescription} />
      </Paragraph>

      <ReviewQueueModeratedPosts moderatedPosts={moderatedPosts} />

      <PaginationWrapper centerAll={true}>
        <Pagination {...pagination} />
      </PaginationWrapper>
    </React.Fragment>
  );
};

export const ReviewQueueBody = compose(
  withProps<WithModeratedPublicationPostsParentProps, ParentProps>(
    (props: ParentProps): WithModeratedPublicationPostsParentProps => {
      const { publication: { oid: publicationOid } } = props;

      return { publicationOid };
    }
  ),
  withQueryParamPaginationController<WithModeratedPublicationPostsProps>(
    withModeratedPublicationPosts,
    // jw: due to the parameter in the route we need to compose this ourselves.
    // tslint:disable-next-line:no-any
    (props: any) => {
      const { publication: { oid, prettyUrlString } } = props;

      const id = getIdForUrl(prettyUrlString, oid);

      return createUrl(generatePath(WebRoute.PublicationReviewQueue, { id }));
    },
    // jw: going to use a query arg for this URL since I prefer that over different routes for each page.
    pageParam
  ),
  withLoadingPlaceholder(fullPlaceholder)
)(ReviewQueueBodyComponent) as React.ComponentClass<ParentProps>;

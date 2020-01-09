import * as React from 'react';
import { ComponentEnhancer, compose, withProps } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import { WebRoute } from '../constants/routes';
import { PaginationConfig } from 'antd/lib/pagination';
import { ExtractedPageableProps, getQueryArg } from '@narrative/shared';
import { Link } from 'react-router-dom';
import { createUrl } from '../utils/routeUtils';

/** Pagination current page HOC
 * This should proceed a pageable graphql query HOC
 */

export interface WithPaginationCurrentPageProps {
  currentPage: number;
}

const withPaginationCurrentPage = compose(
  withProps((props: RouteComponentProps<{page: string}>) => {
    const { match: { params } } = props;

    if (!params.page) {
      return;
    }

    return { currentPage: parseInt(params.page, 10) };
  }),
);

/** Pagination config HOC
 * This will add a default pagination config for an ant list or table component
 * This depends on withPaginationCurrentPage and a pageable graphql query HOC to proceed it
 */
export type ParentProps =
  ExtractedPageableProps &
  WithPaginationCurrentPageProps;

interface WithPaginationConfigProps  {
  pagination: PaginationConfig;
}

// jw: added support to this route property for specifying a resolver function that can be used to derive the route
//     from the properties for the calling component. This is necessary for paths with non-page variables in them.
export const withPaginationConfig = (
// tslint:disable-next-line:no-any
  route: WebRoute | ((props: any) => string),
  queryParamName?: string,
  urlFragment?: string
) => compose(
  withProps((props: ParentProps) => {
    const { pageInfo, pageSize, currentPage } = props;

    let routeResolved: string | WebRoute;
    if (typeof route === 'function') {
      routeResolved = route(props);

    } else {
      routeResolved = route;
    }

    const pagination: PaginationConfig = {
      defaultCurrent: currentPage,
      current: currentPage,
      pageSize,
      total: pageInfo ? pageInfo.totalElements : undefined,
      hideOnSinglePage: true,
      itemRender: (page, _, originalElement) => {
        // originalElement is typed by antd incorrectly (it's actually a React.ReactElement<HTMLAnchorElement>)
        // bug logged with antd: https://github.com/ant-design/ant-design/issues/15384
        // bl: have to coerce the originalElement type into React.ReactElement in order to comply with the contract
        // of React.Children.map().
        // tslint:disable-next-line no-any
        const element: any = originalElement;
        return React.Children.map(element as React.ReactElement, (anchor: React.ReactElement<HTMLAnchorElement>) => {
          let anchorProps = {
            ...anchor.props,
            className: 'ant-pagination-item-link',
            style: {
              ...anchor.props.style,
              width: '100%',
              display: 'inline-block',
              margin: '0',
            }
          };

          let to: string | undefined;

          if (currentPage === page || page < 1) {
            anchorProps = {
              ...anchorProps,
              style: {
                ...anchorProps.style,
                cursor: 'default'
              }
            };
          } else if (queryParamName) {
            to = createUrl(routeResolved, {[queryParamName]: page !== 1 ? page : undefined}, urlFragment);
          } else {
            to = createUrl(`${routeResolved}/${page}`, {}, urlFragment);
          }

          // jw: if we are going to link somewhere
          if (to) {
            // jw: the property definitions from HTMLAnchorProperties to LinkProps from router are misaligned in that
            //     almost all style properties are `[type] | null` on HTMLAnchorProperties but are `[type] | undefined`
            //     on the LinkProps router side... Ignoring the error allows it to render, and I am not seeing any
            //     errors, so leaving well enough alone.
            // @ts-ignore
            return <Link to={to} {...anchorProps} />;
          }

          return React.cloneElement(anchor, anchorProps);
        });
      },
    };

    return { pagination };
  })
);

/** Pagination controller HOC
 * allows you to compose the pagination HOC's with a pageable graphql query HOC
 * the pageable graphql query HOC depends on the withPaginationCurrentPage
 * the withPaginationConfig HOC depends on both withPaginationCurrentPage and the pageable HOC
 */
export type WithPaginationControllerProps =
  WithPaginationCurrentPageProps &
  WithPaginationConfigProps;

export function withPaginationController<GraphqlQueryProps>(
  // takes a pageable graphql query and route as arguments
  graphqlQueryHOC: (wrappedComponent: React.ComponentType<GraphqlQueryProps>) => void,
  // jw: added support to this route property for specifying a resolver function that can be used to derive the route
  //     from the properties for the calling component. This is necessary for paths with non-page variables in them.
  // tslint:disable-next-line:no-any
  route: WebRoute | ((props: ((props: any) => string)) => string),
  queryParamName?: string,
  urlFragment?: string
): ComponentEnhancer<GraphqlQueryProps, GraphqlQueryProps & WithPaginationControllerProps> {
  return compose(
    withPaginationCurrentPage,
    graphqlQueryHOC,
    withPaginationConfig(route, queryParamName, urlFragment)
  );
}

/**
 * Extension of the above HOC that removes the route current page extraction and instead builds in a tool for pulling
 * it out of the query parameters
 */
export function withQueryParamPaginationController<GraphqlQueryProps>(
  // takes a pageable graphql query and route as arguments
  graphqlQueryHOC: (wrappedComponent: React.ComponentType<GraphqlQueryProps>) => void,
  // jw: added support to this route property for specifying a resolver function that can be used to derive the route
  //     from the properties for the calling component. This is necessary for paths with non-page variables in them.
  // tslint:disable-next-line:no-any
  route: WebRoute | ((props: ((props: any) => string)) => string),
  queryParamName: string,
  urlFragment?: string
): ComponentEnhancer<GraphqlQueryProps, GraphqlQueryProps & WithPaginationControllerProps> {
  return compose(
    withRouter,
    // jw: let's resolve the currentPage from query args
    withProps((props: RouteComponentProps<{}>) => {
      const { location: { search } } = props;

      const page = getQueryArg(search, queryParamName);

      // jw: always default to the first page.
      const currentPage = page && parseInt(page, 10) || 1;

      return { currentPage };
    }),
    graphqlQueryHOC,
    withPaginationConfig(route, queryParamName, urlFragment)
  );
}

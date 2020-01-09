import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { AgeRating, CurrentUserPermittedAgeRatingQuery } from '../../types';
import { currentUserPermittedAgeRatingQuery } from '../graphql/user/currentUserPermittedAgeRating';

const queryName = 'currentUserPermittedAgeRatingData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & CurrentUserPermittedAgeRatingQuery},
  WithCurrentUserPermittedAgeRatingProps
  >;

export type WithCurrentUserPermittedAgeRatingProps =
  ChildDataProps<{}, CurrentUserPermittedAgeRatingQuery> & {
  permittedAgeRatingLoading: boolean;
  permittedAgeRatings: AgeRating[];
};

export const withCurrentUserPermittedAgeRating =
  graphql<
    {},
    CurrentUserPermittedAgeRatingQuery,
    {},
    WithCurrentUserPermittedAgeRatingProps
    >(currentUserPermittedAgeRatingQuery, {
    name: queryName,
    props: ({ currentUserPermittedAgeRatingData, ownProps }: WithProps) => {
      const { loading, getCurrentUserPermittedAgeRating } = currentUserPermittedAgeRatingData;

      const permittedAgeRatings =
        getCurrentUserPermittedAgeRating &&
        getCurrentUserPermittedAgeRating.permittedAgeRatings || [];

      return {
        ...ownProps,
        permittedAgeRatingLoading: loading,
        permittedAgeRatings
      };
    }
  });

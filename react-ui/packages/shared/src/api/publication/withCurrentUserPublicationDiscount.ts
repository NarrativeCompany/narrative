import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { getCurrentUserPublicationDiscountQuery } from '../graphql/publication/currentUserPublicationDiscount';
import { CurrentUserPublicationDiscountQuery } from '../../types';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithCurrentUserPublicationDiscountProps extends LoadingProps {
  eligibleForDiscount?: boolean;
}

const queryName = 'currentUserPublicationDiscountData';

type Props = NamedProps<{[queryName]: GraphqlQueryControls & CurrentUserPublicationDiscountQuery}, {}>;

export const withCurrentUserPublicationDiscount = graphql<
  {},
  CurrentUserPublicationDiscountQuery,
  {},
  WithCurrentUserPublicationDiscountProps
  >(getCurrentUserPublicationDiscountQuery, {
  name: queryName,
    props: ({ currentUserPublicationDiscountData }: Props): WithCurrentUserPublicationDiscountProps => {
      const { loading, getCurrentUserPublicationDiscount } = currentUserPublicationDiscountData;
      const eligibleForDiscount = getCurrentUserPublicationDiscount &&
        getCurrentUserPublicationDiscount.eligibleForDiscount;

      return { loading, eligibleForDiscount };
    }
});

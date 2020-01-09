import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { CurrentUserEmailAddressQuery, UserEmailAddressDetail } from '../../types';
import { currentUserEmailAddressQuery } from '../graphql/user/currentUserEmailAddressQuery';
import { LoadingProps } from '../../utils';

// jw: per new standards, let's trim the fat on what we are exposing and limit it to just what we care about:
export interface WithCurrentUserEmailAddressProps extends LoadingProps {
  emailAddressDetail: UserEmailAddressDetail;
}

const queryName = 'currentUserEmailAddressData';

type Props =
  NamedProps<{[queryName]: GraphqlQueryControls & CurrentUserEmailAddressQuery}, {}>;

export const withCurrentUserEmailAddress =
  graphql<{}, CurrentUserEmailAddressQuery, {}, WithCurrentUserEmailAddressProps>(currentUserEmailAddressQuery,
    {
      name: queryName,
      props: ({ currentUserEmailAddressData }: Props): WithCurrentUserEmailAddressProps => {
        const { loading } = currentUserEmailAddressData;
        const emailAddressDetail = currentUserEmailAddressData.getCurrentUserEmailAddress;

        return { loading, emailAddressDetail };
      }
    }
  );

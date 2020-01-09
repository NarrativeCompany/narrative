import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { countryListQuery } from '../graphql/shared/countryListQuery';
import { Country, CountryListQuery } from '../../types';

const queryName = 'countryListData';

interface ParentProps {
  postOid?: string;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & CountryListQuery},
  WithCountryListProps
>;

export type WithCountryListProps =
  ChildDataProps<ParentProps, CountryListQuery> & {
  countryListLoading: boolean;
  countryList: Country[];
};

export const withCountryList =
  graphql<
    ParentProps,
    CountryListQuery,
    {},
    WithCountryListProps
  >(countryListQuery, {
    name: queryName,
    props: ({ countryListData, ownProps }: WithProps) => {
      const { loading, getCountryList } = countryListData;

      return {
        ...ownProps,
        countryListLoading: loading,
        countryList: getCountryList || []
      };
    }
  });

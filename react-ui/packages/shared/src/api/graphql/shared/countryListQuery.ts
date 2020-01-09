import gql from 'graphql-tag';
import { CountryFragment } from '../fragments/countryFragment';

export const countryListQuery = gql`
  query CountryListQuery {
    getCountryList @rest(type: "Country", path: "/canonical/countries") {
      ...Country
    }
  }
  ${CountryFragment}
`;

import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { personalizedContentStreamQuery } from '../graphql/contentStream/personalizedContentStreamQuery';
import { PersonalizedContentStreamQuery } from '../../types';
import {
  ContentStreamFilters,
  createContentStreamPropsFromQueryResults,
  extractContentStreamFilters,
  WithContentStreamProps
} from './contentStreamUtils';
import { infiniteLoadingFixProps } from '../../utils';

const queryName = 'personalizedContentStreamData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & PersonalizedContentStreamQuery},
  ChildDataProps<ContentStreamFilters, PersonalizedContentStreamQuery>
>;

export const withPersonalizedContentStream =
  graphql<
    ContentStreamFilters,
    PersonalizedContentStreamQuery,
    {},
    WithContentStreamProps
  >(personalizedContentStreamQuery, {
    options: (props: ContentStreamFilters) => ({
      ...infiniteLoadingFixProps,
      variables: {
        filters: extractContentStreamFilters(props)
      }
    }),
    name: queryName,
    props: ({ personalizedContentStreamData }: WithProps) => {
      return createContentStreamPropsFromQueryResults('getPersonalizedContentStream', personalizedContentStreamData);
    }
  });

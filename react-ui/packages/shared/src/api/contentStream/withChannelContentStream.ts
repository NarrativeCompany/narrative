import { ChildDataProps, graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { channelContentStreamQuery } from '../graphql/contentStream/channelContentStreamQuery';
import { ChannelContentStreamQuery, ContentStreamChannelType } from '../../types';
import {
  ContentStreamFilters,
  createContentStreamPropsFromQueryResults,
  extractContentStreamFilters,
  WithContentStreamProps
} from './contentStreamUtils';
import { infiniteLoadingFixProps } from '../../utils';

const queryName = 'channelContentStreamData';

export interface WithChannelContentStreamParentProps extends ContentStreamFilters {
  channelType: ContentStreamChannelType;
  channelOid: string;
  nicheOid?: string;
}

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & ChannelContentStreamQuery},
  ChildDataProps<WithChannelContentStreamParentProps, ChannelContentStreamQuery>
>;

export const withChannelContentStream =
  graphql<
    WithChannelContentStreamParentProps,
    ChannelContentStreamQuery,
    {},
    WithContentStreamProps
  >(channelContentStreamQuery, {
    options: ({ channelType, channelOid, nicheOid, ...filters }: WithChannelContentStreamParentProps) => ({
      ...infiniteLoadingFixProps,
      variables: {
        channel: {type: channelType, oid: channelOid},
        nicheFilters: { nicheOid },
        filters: extractContentStreamFilters(filters)
      }
    }),
    name: queryName,
    props: ({ channelContentStreamData }: WithProps) => {
      return createContentStreamPropsFromQueryResults('getChannelContentStream', channelContentStreamData);
    }
  });

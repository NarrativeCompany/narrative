import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { shutdownNoticeUrlQuery } from '../graphql/shared/shutdownNoticeUrlQuery';
import { ShutdownNoticeUrlQuery } from '../../types';

const queryName = 'shutdownNoticeUrlData';

type WithProps = NamedProps<
  {[queryName]: GraphqlQueryControls & ShutdownNoticeUrlQuery},
  WithShutdownNoticeUrlProps
>;

export interface WithShutdownNoticeUrlProps {
  shutdownNoticeUrl?: string;
}

export const withShutdownNoticeUrl = graphql<
    {},
    ShutdownNoticeUrlQuery,
    {},
    WithShutdownNoticeUrlProps
  >(shutdownNoticeUrlQuery, {
  name: queryName,
  props: ({ shutdownNoticeUrlData }: WithProps) => {
      const { getShutdownNoticeUrl } = shutdownNoticeUrlData;

      const shutdownNoticeUrl = getShutdownNoticeUrl && getShutdownNoticeUrl.shutdownNoticeUrl || undefined;

      return {
        shutdownNoticeUrl
      };
    }
});

import * as React from 'react';
import { compose, withProps } from 'recompose';
import {
  Niche,
  withSimilarNichesByNicheId,
  WithSimilarNichesByNicheIdProps
} from '@narrative/shared';
import { SidebarCard } from '../SidebarCard';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { FormattedMessage } from 'react-intl';
import { NicheLink } from '../niche/NicheLink';
import { Tag } from '../Tag';
import { Paragraph } from '../Paragraph';
import { EnhancedNicheStatus } from '../../enhancedEnums/nicheStatus';
import { Tooltip } from 'antd';

interface ParentProps {
  niche: Niche;
}

interface Props {
  similarNiches: Niche[];
  loading: boolean;
}

const SimilarNichesSidebarCardComponent: React.SFC<Props> = (props) => {
  const { loading, similarNiches } = props;

  if (loading) {
    return (
      <SidebarCard
        title={<FormattedMessage {...SidebarMessages.SimilarNiches} />}
        loading={true}
      />
    );
  }

  return (
    <SidebarCard title={<FormattedMessage {...SidebarMessages.SimilarNiches} />}>
      {similarNiches.length
        ? similarNiches.map(niche => {
          const status = EnhancedNicheStatus.get(niche.status);

          return (
            <NicheLink key={niche.oid} niche={niche}>
              <Tooltip placement="top" title={<FormattedMessage {...status.message}/>}>
                <Tag size="normal" margin="small" color={status.tagColor}>{niche.name}</Tag>
              </Tooltip>
            </NicheLink>
          );
        })
        : <Paragraph textAlign="center" size="large">
            <FormattedMessage {...SidebarMessages.None} />
          </Paragraph>
      }
    </SidebarCard>
  );
};

export const SimilarNichesSidebarCard = compose(
  // jw: extract the nicheId off of the niche for the query
  withProps((props: ParentProps) => {
    return { nicheId: props.niche.oid };
  }),
  withSimilarNichesByNicheId,
  // jw: extract the relevant properties off of the query results.
  withProps((props: WithSimilarNichesByNicheIdProps) => {
    const { similarNichesByNicheIdData: { loading, getSimilarNichesByNicheId } } = props;

    const similarNiches = getSimilarNichesByNicheId || [];

    return { loading, similarNiches };
  })
)(SimilarNichesSidebarCardComponent) as React.ComponentClass<ParentProps>;

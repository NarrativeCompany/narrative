import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import {
  Publication,
  withPublicationTopNiches,
  WithPublicationTopNichesParentProps,
  WithPublicationTopNichesProps
} from '@narrative/shared';
import { ChannelsContainer } from '../../../../../shared/components/contentStream/ContentStreamItem';
import { Tag } from '../../../../../shared/components/Tag';
import { getChannelUrl } from '../../../../../shared/utils/channelUtils';
import { renderTopNicheLink } from './PublicationTopNichesSidebarItem';

interface ParentProps {
  publication: Publication;
}

type Props = ParentProps &
  WithPublicationTopNichesProps;

const PublicationTopNichesHeaderComponent: React.SFC<Props> = (props) => {
  const { publication, publicationTopNiches } = props;

  // jw: let's derive this once and then use it for each niche link below.
  const urlBase = getChannelUrl(publication);

  return (
    <ChannelsContainer>
      {publicationTopNiches.map(topNiche =>
        <Tag size="normal" key={topNiche.oid}>{renderTopNicheLink(topNiche, urlBase)}</Tag>
      )}
    </ChannelsContainer>
  );
};

export const PublicationTopNichesHeader = compose(
  withProps<WithPublicationTopNichesParentProps, ParentProps>((props: ParentProps) => {
    const publicationOid = props.publication.oid;

    return { publicationOid };
  }),
  withPublicationTopNiches,
  // jw: if there are no top niches we will not display anything at the top, so let's not include a loading indicator
  branch<WithPublicationTopNichesProps>(props => props.loading || props.publicationTopNiches.length === 0,
    renderComponent(() => null)
  )
)(PublicationTopNichesHeaderComponent) as React.ComponentClass<ParentProps>;

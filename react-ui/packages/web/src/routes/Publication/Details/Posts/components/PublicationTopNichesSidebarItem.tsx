import * as React from 'react';
import { branch, compose, renderComponent, withProps } from 'recompose';
import { Publication, withPublicationTopNiches, WithPublicationTopNichesProps, TopNiche } from '@narrative/shared';
import { Link } from '../../../../../shared/components/Link';
import { FormattedMessage } from 'react-intl';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { getChannelUrl } from '../../../../../shared/utils/channelUtils';
import { BasicCard } from '../../../../../shared/components/card/BasicCard';
import { createNicheFilteredContentStreamUrl } from '../../../../../shared/utils/publicationUtils';

interface ParentProps {
  publication: Publication;
}

type Props =
  ParentProps &
  WithPublicationTopNichesProps;

export const renderTopNicheLink = (topNiche: TopNiche, urlBase: string): React.ReactNode => {
  const { oid, name } = topNiche;

  return <Link to={createNicheFilteredContentStreamUrl(urlBase, oid)}>{name}</Link>;
};

const PublicationTopNichesSidebarItemComponent: React.SFC<Props> = (props) => {
  const { publication, publicationTopNiches } = props;

  // jw: let's derive this once and then use it for each niche link below.
  const urlBase = getChannelUrl(publication);

  const renderTopNiche = (topNiche: TopNiche, index: number): React.ReactNode => {
    const { oid } = topNiche;

    return (
      <React.Fragment key={`topNiche_${oid}`}>
        {index === 0 ? null : ', '}
        {renderTopNicheLink(topNiche, urlBase)}
      </React.Fragment>
    );
  };

  // jw: because of the compilation stack we will only be here if we have top niches.
  return (
    <BasicCard title={<FormattedMessage {...PublicationDetailsMessages.PopularNiches} />}>
      {publicationTopNiches.map((topNiche: TopNiche, index: number) => renderTopNiche(topNiche, index))}
    </BasicCard>
  );
};

export const PublicationTopNichesSidebarItem = compose(
  withProps((props: ParentProps) => {
    const publicationOid = props.publication.oid;

    return { publicationOid };
  }),
  withPublicationTopNiches,
  branch((props: WithPublicationTopNichesProps) => props.loading || props.publicationTopNiches.length === 0,
    renderComponent(() => null)
  )
)(PublicationTopNichesSidebarItemComponent) as React.ComponentClass<ParentProps>;

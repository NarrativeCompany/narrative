import * as React from 'react';
import { branch, compose, renderNothing, withProps } from 'recompose';
import {
  Niche,
  NicheAssociationType,
  withNicheUserAssociations,
  WithNicheUserAssociationsProps
} from '@narrative/shared';
import { SidebarMessages } from '../../i18n/SidebarMessages';
import { FormattedMessage } from 'react-intl';
import { NicheLink } from '../niche/NicheLink';
import { Tag } from '../Tag';
import { WithCurrentUserProps, withExtractedCurrentUser } from '../../containers/withExtractedCurrentUser';
import { LoadingProps } from '../../utils/withLoadingPlaceholder';
import { SectionHeader } from '../SectionHeader';
import { Block } from '../Block';

interface OwnedNichesSidebarCardProps {
  ownedNiches: Niche[];
}

const OwnedNichesSidebarItemComponent: React.SFC<OwnedNichesSidebarCardProps> = (props) => {
  const { ownedNiches } = props;

  return (
    <Block style={{marginBottom: 30}}>
      <SectionHeader title={<FormattedMessage {...SidebarMessages.OwnedNiches} />}/>
      {ownedNiches.map(niche => (
        <NicheLink key={niche.oid} niche={niche}>
          <Tag size="normal" margin="small">{niche.name}</Tag>
        </NicheLink>
      ))}
    </Block>
  );
};

export const OwnedNichesSidebarItem = compose(
  withExtractedCurrentUser,
  // jw: by getting here we know that we have a current user, so let's use the property set that assumes that.
  withProps((props: WithCurrentUserProps) => {
    return { userOid: props.currentUser.oid };
  }),
  withNicheUserAssociations,
  // jw: we want to render nothing when loading because if the user does not own any niches this widget will not be
  //     rendered, so we do not want to render a placeholder and then just remove it.
  branch((props: LoadingProps) => props.loading,
    renderNothing
  ),

  // jw: now that we are not loading, let's go ahead and parse out the owned niches
  withProps((props: WithNicheUserAssociationsProps) => {
    const { associations } = props;

    const ownedAssociations = associations.filter(association => association.type === NicheAssociationType.OWNER);
    const ownedNiches = Array.from(ownedAssociations, association => association.niche);

    return { ownedNiches };
  }),
  // jw: Similar to above, if we do not have any owned niches at this point, then just render null
  branch((props: OwnedNichesSidebarCardProps) => !props.ownedNiches || !props.ownedNiches.length,
    renderNothing
  ),
)(OwnedNichesSidebarItemComponent) as React.ComponentClass<{}>;

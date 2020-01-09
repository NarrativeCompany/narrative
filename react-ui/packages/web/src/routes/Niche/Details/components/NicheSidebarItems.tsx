import * as React from 'react';
import { compose } from 'recompose';
import { NicheOwnerSidebarItem } from './NicheOwnerSidebarItem';
import { EnhancedNicheStatus } from '../../../../shared/enhancedEnums/nicheStatus';
import {
  NicheDetailsConnect,
  WithNicheDetailsContextProps
} from './NicheDetailsContext';
import { NicheModeratorsSidebarItem } from './NicheModeratorsSidebarItem';
import { SimilarNichesSidebarCard } from '../../../../shared/components/sidebar/SimilarNichesSidebarCard';
import { FeaturedPostsSidebarItem } from '../../../../shared/components/sidebar/FeaturedPostSidebarItems';

const NicheSidebarItemsComponent: React.SFC<WithNicheDetailsContextProps> = (props) => {
  const { nicheDetail } = props;

  const nicheStatus = EnhancedNicheStatus.get(nicheDetail.niche.status);

  if (!nicheStatus.isActive()) {
    return <SimilarNichesSidebarCard niche={nicheDetail.niche}/>;
  }

  return (
    <React.Fragment>
      <NicheOwnerSidebarItem {...props}/>
      <NicheModeratorsSidebarItem {...props}/>
      <FeaturedPostsSidebarItem/>
    </React.Fragment>
  );
};

export const NicheSidebarItems = compose(
  NicheDetailsConnect
)(NicheSidebarItemsComponent) as React.ComponentClass<{}>;

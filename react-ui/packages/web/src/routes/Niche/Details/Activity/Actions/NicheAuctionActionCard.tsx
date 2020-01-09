import * as React from 'react';
import { compose } from 'recompose';
import { AuctionOidProps } from '@narrative/shared';
import {
  DetailsActionPlaceholderCard
} from '../../../../../shared/components/detailAction/DetailsActionPlaceholderCard';
import { FormattedMessage } from 'react-intl';
import { NicheDetailsMessages } from '../../../../../shared/i18n/NicheDetailsMessages';
import { AuctionActionCard } from '../../../../../shared/components/auction/AuctionActionCard';
import {
  withUpdateableAuctionDetail,
  WithUpdateableAuctionDetailProps
} from '../../../../../shared/containers/withUpdateableAuctionDetail';

const NicheAuctionActionCardComponent: React.SFC<WithUpdateableAuctionDetailProps> = (props) => {
  // jw: if we failed to find a auction from the server, let's output nothing.
  if (!props.auction) {
    // todo:error-handling: we need to report this to the server, so that we can track down how this happened. We
    //      never delete Auctions, so this should never ever happen!
    return null;
  }

  return (
    <AuctionActionCard
      {...props}
      footerText={<FormattedMessage {...NicheDetailsMessages.AuctionDetails} />}
    />
  );
};

export const NicheAuctionActionCard = compose(
  withUpdateableAuctionDetail(() => <DetailsActionPlaceholderCard />)
)(NicheAuctionActionCardComponent) as React.ComponentClass<AuctionOidProps>;

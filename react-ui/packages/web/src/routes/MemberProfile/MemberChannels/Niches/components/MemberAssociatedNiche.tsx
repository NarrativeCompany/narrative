import * as React from 'react';
import { MemberChannelsMessages } from '../../../../../shared/i18n/MemberChannelsMessages';
import { FormattedMessage } from 'react-intl';
import {
  NicheUserAssociation,
  NicheStatus
} from '@narrative/shared';
import { LocalizedTime } from '../../../../../shared/components/LocalizedTime';
import { MemberAssociatedChannel } from '../../components/MemberAssociatedChannel';
import { ChannelStatusTag } from '../../../../../shared/components/channel/ChannelStatusTag';

interface ParentProps {
  association: NicheUserAssociation;
}

export const MemberAssociatedNiche: React.SFC<ParentProps> = (props) => {
  const { association } = props;
  const { niche, associationDatetime } = association;

  let purchaseDatetime: React.ReactNode | undefined;
  if (niche.status === NicheStatus.ACTIVE) {
    const formattedAssociationDatetime = <LocalizedTime time={associationDatetime}/>;

    purchaseDatetime = (
      <FormattedMessage
        {...MemberChannelsMessages.Purchased}
        values={{purchaseDatetime: formattedAssociationDatetime}}
      />
    );
  }

  return (
    <MemberAssociatedChannel
      channel={niche}
      afterName={<ChannelStatusTag size="small" marginLeft="small" channel={niche}/>}
      subTitle={purchaseDatetime}
      description={niche.description}
    />
  );
};

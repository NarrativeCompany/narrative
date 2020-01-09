import * as React from 'react';
import styled from '../../styled';
import { theme } from '../../styled';
import { NicheGeneralMessages } from '../../i18n/NicheGeneralMessages';
import { FormattedMessage } from 'react-intl';
import { Niche, NicheStatus, DeletedChannel } from '@narrative/shared';
import { isOfType } from '../../../apolloClientInit';
import { ChannelMessages } from '../../i18n/ChannelMessages';
import { Channel } from '../../utils/channelUtils';

// jw: these properties will be shared by the shared tag and the external component. So let's define this first.
interface SharedProps {
  size?: 'small' | 'large';
  marginLeft?: 'small' | 'large';
}

// jw: let's define the properties that will be consumed internally by our shared tag styled component.
interface BaseTagPropsInterface {
  borderColor: string;
  backgroundColor: string;
  color: string;
}

// jw: finally, let's compile the shared external properties with the internal tag props.
type BaseTagProps = BaseTagPropsInterface &
  SharedProps;

// jw: now that we have the compiled properties definition for the shared tag, let's create functions to use that.
function getPadding(props: SharedProps) {
  const { size } = props;

  if (size === 'small') {
    return '2px 10px';
  }
  if (size === 'large') {
    return '6px 12px';
  }
  return '4px 10px';
}

function getFontSize(props: SharedProps) {
  const { size } = props;

  if (size === 'small') {
    return '10px';
  }
  if (size === 'large') {
    return '16px';
  }
  return '12px';
}

function getMarginLeft(props: SharedProps) {
  const { marginLeft } = props;

  if (marginLeft === 'small') {
    return '4px';
  }
  if (marginLeft === 'large') {
    return '10px';
  }
  return '0';
}

// jw: Phew, finally we are ready to define this shared styled component.
const TagBase = styled.span<BaseTagProps>`
  border: 1px solid ${props => props.borderColor};
  border-radius: 4px;
  color: ${props => props.color};
  background-color: ${props => props.backgroundColor};
  padding: ${props => getPadding(props)};
  font-size: ${props => getFontSize(props)};
  margin-left: ${props => getMarginLeft(props)};
  text-transform: uppercase;
  white-space: nowrap;
  display: flex;
  align-items: center;
  justify-content: center;
`;

// jw: now that we have that done, let's create the few versions of the TagBase.
// note: The SharedProps are all we will consume, the BaseTagProps will be defined for each of these directly.
const RedTag = styled<SharedProps>((props) =>
  <TagBase borderColor={theme.primaryRed} backgroundColor="#ffe6e6" color={theme.primaryRed} {...props}/>)``;

const GreenTag = styled<SharedProps>((props) =>
  <TagBase borderColor={theme.primaryGreen} backgroundColor="#e6fffa" color={theme.primaryGreen} {...props}/>)``;

const BeigeTag = styled<SharedProps>((props) =>
  <TagBase borderColor="#A5850B" backgroundColor="#FDF8E4" color="#D0BE75" {...props}/>)``;

const GreyTag = styled<SharedProps>((props) => (
  <TagBase
    borderColor={theme.defaultTagColor}
    backgroundColor={theme.defaultTagBackgroundColor}
    color={theme.defaultTagColor}
    {...props}
  />
))``;

// jw: now, we are ready to define our external Component. Let's start with the props that we will consume and use.
interface ParentProps {
  channel: Channel | DeletedChannel;
  style?: React.CSSProperties;
}

// jw: now, let's create a type aggregating the properties specific to the external ChannelStatusTag, and add the
//     SharedProps to that.
type Props =
  ParentProps &
  SharedProps;

// jw: Finally, let's create the external ChannelStatusTag
export const ChannelStatusTag: React.SFC<Props> = (props) => {
  // jw: this is important, after we define the channel, the only properties left will be from the SharedProps which
  //     will be consumed by the various TagBase implementations. So, let's grab the channel and create a object
  //     containing all of those other properties, so we can just spread them onto the TagBases. This makes it
  //     trivial to add new SharedProps and have them "just work".
  const { channel, ...tagProps } = props;

  if (isOfType(channel, 'Niche')) {
    const niche = channel as Niche;
    switch (niche.status) {
      case NicheStatus.ACTIVE:
        return <GreenTag {...tagProps}>{niche.status}</GreenTag>;

      case NicheStatus.REJECTED:
        return <RedTag {...tagProps}>{niche.status}</RedTag>;

      case NicheStatus.FOR_SALE:
        return <BeigeTag {...tagProps}><FormattedMessage {...NicheGeneralMessages.UpForAuction}/></BeigeTag>;

      case NicheStatus.PENDING_PAYMENT:
        return <BeigeTag {...tagProps}><FormattedMessage {...NicheGeneralMessages.PendingPayment}/></BeigeTag>;

      default:
        return <GreyTag {...tagProps}>{niche.status}</GreyTag>;
    }
  }

  if (isOfType(channel, 'Publication')) {
    // bl: the only status for Publications is active
    return <GreenTag {...tagProps}><FormattedMessage {...ChannelMessages.ActiveStatus}/></GreenTag>;
  }

  // bl: the only status for DeletedChannels is deleted
  return <RedTag {...tagProps}><FormattedMessage {...ChannelMessages.DeletedStatus}/></RedTag>;
};

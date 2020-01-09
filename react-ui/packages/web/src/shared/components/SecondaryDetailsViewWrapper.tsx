import * as React from 'react';
import { DetailsViewWrapper, DetailsViewWrapperProps } from './DetailsViewWrapper';
import { Card } from './Card';
import { IconType } from './CustomIcon';
import { PageHeader } from './PageHeader';
import styled from '../styled';
import { FormattedMessage } from 'react-intl';
import { Omit } from 'recompose';
import { Channel } from '../utils/channelUtils';
import { CSSProperties } from 'react';
import { DeletedChannel } from '@narrative/shared';

const StyledStatus = styled.span`
  color: ${props => props.theme.primaryRed};
`;

interface Props extends Omit<DetailsViewWrapperProps, 'gradientBoxColor'> {
  channel?: Channel;
  deletedChannel?: DeletedChannel;
  iconType: IconType;
  title: string | React.ReactNode;
  listLink: React.ReactNode;
  status?: FormattedMessage.MessageDescriptor;
}

const cardStyle: CSSProperties = {width: '100%', marginBottom: '75px'};

export const SecondaryDetailsViewWrapper: React.SFC<Props> = (props) => {
  const {channel, deletedChannel, iconType, title, listLink, status, ...wrapperProps } = props;

  return (
    <DetailsViewWrapper {...wrapperProps} gradientBoxColor="blue">
      <Card style={{width: '100%'}}>
        <PageHeader
          iconType={iconType}
          title={title}
          titleHelper={listLink}
          extra={status && <StyledStatus><FormattedMessage {...status} /></StyledStatus>}
        />

        <Card.Channel
          style={cardStyle}
          channel={channel}
          deletedChannel={deletedChannel}
          includeStatus={true}
        />

        {props.children}
      </Card>
    </DetailsViewWrapper>
  );
};

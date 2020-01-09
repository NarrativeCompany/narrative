import * as React from 'react';
import { ReactNode } from 'react';
import { Icon } from 'antd';
import { FormattedMessage } from 'react-intl';
import { Heading } from '../../../shared/components/Heading';
import { PostMessages } from '../../../shared/i18n/PostMessages';
import { Niche, Publication } from '@narrative/shared';
import styled from '../../../shared/styled';
import { ChannelLink } from '../../../shared/components/channel/ChannelLink';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';

const PostLocationWrapper =
  styled<
    {status: ConnectionStatus} &
    FlexContainerProps
  >(({status, ...rest}) => <FlexContainer {...rest}/>)`
    margin-bottom: 35px;
    
    h4 {
      margin: 0 auto 0 0;
      line-height: 1.5;
    }
    
    .anticon {
      margin-right: 10px;
      font-size: 18px;
    }
    
    span,
    .anticon {
      color: ${p => p.status === 'connected' ? p.theme.brightGreen : p.theme.primaryOrange};
    }
  `;

const connectionStatusProps = {
  connected: {
    iconType: 'check',
    text: PostMessages.ConnectionLocationLive
  },
  pending: {
    iconType: 'clock-circle',
    text: PostMessages.ConnectionLocationPending
  }
};

export type ConnectionStatus = keyof typeof connectionStatusProps;

interface ParentProps {
  niche?: Niche;
  publication?: Publication;
  personalJournal?: boolean;
  status: ConnectionStatus;
}

export const PostLocation: React.SFC<ParentProps> = (props) => {
  const { niche, publication, personalJournal } = props;

  const status = connectionStatusProps[props.status];

  let heading: string | ReactNode = '';

  if (personalJournal) {
    heading = <FormattedMessage {...PostMessages.JournalLabel}/>;
  } else if (niche || publication) {
    heading = <ChannelLink channel={niche || publication} color="default"/>;
  }

  return (
    <PostLocationWrapper alignItems="center" status={props.status}>
      <Heading size={4}>{heading}</Heading>

      <Icon type={status.iconType}/>
      <FormattedMessage {...status.text}/>
    </PostLocationWrapper>
  );
};

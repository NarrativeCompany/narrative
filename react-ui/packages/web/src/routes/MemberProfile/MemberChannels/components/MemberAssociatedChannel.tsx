import * as React from 'react';
import styled from '../../../../shared/styled';
import { Paragraph } from '../../../../shared/components/Paragraph';
import { FlexContainer } from '../../../../shared/styled/shared/containers';
import { Channel } from '../../../../shared/utils/channelUtils';
import { ChannelLink } from '../../../../shared/components/channel/ChannelLink';
import { Col, Row } from 'antd';

interface Props {
  channel: Channel;
  afterName?: React.ReactNode;
  subTitle?: React.ReactNode;
  description: React.ReactNode;
  avatar?: React.ReactNode;
}

const Container = styled.div`
  padding-bottom: 15px;
  margin-bottom: 15px;
  
  &:not(:last-child) {
    border-bottom: 1px solid ${props => props.theme.defaultTagColor};
  }
`;

const DescriptionContainer = styled(Paragraph)`
  margin-top: 10px;
`;

export const associatedChannelAvatarSize = 80;

export const MemberAssociatedChannel: React.SFC<Props> = (props) => {
  const { channel, afterName, subTitle, description, avatar } = props;

  let subTitleHtml: React.ReactNode | undefined;
  if (subTitle) {
    subTitleHtml = (
      <Paragraph color="light" size="small">
        {subTitle}
      </Paragraph>
    );
  }

  const body = (
    <React.Fragment>
      <FlexContainer>
        <ChannelLink size="large" channel={channel} color="default" />
        {afterName}
      </FlexContainer>
      {subTitleHtml}
      <DescriptionContainer color="light">
        {description}
      </DescriptionContainer>
    </React.Fragment>
  );

  if (!avatar) {
    return (
      <Container>
        {body}
      </Container>
    );
  }

  return (
    <Container>
      <Row type="flex" gutter={20}>
        <Col sm={20}>
          {body}
        </Col>
        <Col sm={4} style={{textAlign: 'right'}}>
          {avatar}
        </Col>
      </Row>
    </Container>
  );
};

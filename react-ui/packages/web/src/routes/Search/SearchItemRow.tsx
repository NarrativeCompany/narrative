import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../shared/styled/shared/containers';
import styled from '../../shared/styled';
import { Col, Divider, Row } from 'antd';
import { Block } from '../../shared/components/Block';
import { FormattedMessage } from 'react-intl';

interface Props {
  type?: FormattedMessage.MessageDescriptor;
  avatar?: React.ReactNode;
  name: string | React.ReactNode;
  description: string | React.ReactNode;
}

const Content = styled.div`
  text-align: left;
`;

const ContentDescription = styled<FlexContainerProps>(FlexContainer)`
  font-size: ${props => props.theme.textFontSizeDefault};
`;

export const SearchItemRow: React.SFC<Props> = (props) => {
  const { type, avatar, name, description } = props;

  const details = (
    <Content>
      {type &&
        <Block color="warning" transform="uppercase">
          <FormattedMessage {...type} />
        </Block>
      }
      <div>
        {name}
      </div>
      <ContentDescription>
        {description}
      </ContentDescription>
    </Content>
  );

  if (avatar) {
    return (
      <React.Fragment>
        <Row type="flex" gutter={20}>
          <Col sm={20}>
            {details}
          </Col>
          <Col sm={4} style={{textAlign: 'right'}}>
            {avatar}
          </Col>
        </Row>
        <Divider />
      </React.Fragment>
    );

  }

  return (
    <React.Fragment>
      {details}
      <Divider />
    </React.Fragment>
  );
};

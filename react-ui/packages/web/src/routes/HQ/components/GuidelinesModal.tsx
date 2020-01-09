import * as React from 'react';
import { Modal, Icon } from 'antd';
import { IconProps } from 'antd/lib/icon';
import { Heading } from '../../../shared/components/Heading';
import { Paragraph } from '../../../shared/components/Paragraph';
import { FormattedMessage } from 'react-intl';
import { GuidelinesModalMessages } from '../../../shared/i18n/GuidelinesModalMessages';
import styled from '../../../shared/styled/index';

const SectionWrapper = styled.div`
  margin-bottom: 30px;
`;

const ArrowIcon = styled<{ color: string; } & IconProps>(({ color, ...rest }) => <Icon {...rest}/>)`
  font-size: 24px;
  position: relative;
  top: 3px;
  color: ${props => props.color === 'blue' ? props.theme.primaryBlue : props.theme.primaryRed};
`;

// tslint:disable no-any
interface ParentProps {
  dismiss: () => any;
  visible: boolean;
}
// tslint:enable no-any

export const GuidelinesModal: React.SFC<ParentProps> = (props) => (
  <Modal
    title={(
      <Heading size={3} noMargin={true}>
        <FormattedMessage {...GuidelinesModalMessages.Title}/>
      </Heading>
    )}
    onCancel={props.dismiss}
    visible={props.visible}
    footer={null}
    width={600}
  >
    <SectionWrapper>
      <Paragraph>
        <FormattedMessage {...GuidelinesModalMessages.ParagraphOne}/>
      </Paragraph>
    </SectionWrapper>

    <SectionWrapper>
      <Heading size={4} uppercase={true}>
        <FormattedMessage
          values={{arrowIcon: (<ArrowIcon type="up" color="blue"/>)}}
          {...GuidelinesModalMessages.SectionOneHeader}
        />
      </Heading>

      <Paragraph>
        <FormattedMessage {...GuidelinesModalMessages.SectionOneParagraphOne}/>
      </Paragraph>
      <br/>
      <Paragraph>
        <FormattedMessage {...GuidelinesModalMessages.SectionOneParagraphTwo}/>
      </Paragraph>
    </SectionWrapper>

    <SectionWrapper>
      <Heading size={4} uppercase={true}>
        <FormattedMessage
          values={{arrowIcon: (<ArrowIcon type="down" color="red"/>)}}
          {...GuidelinesModalMessages.SectionTwoHeader}
        />
      </Heading>

      <Paragraph>
        <FormattedMessage {...GuidelinesModalMessages.SectionTwoParagraphOne}/>
      </Paragraph>
      <br/>
      <Paragraph>
        <FormattedMessage {...GuidelinesModalMessages.SectionTwoParagraphTwo}/>
      </Paragraph>
      <br/>
      <Paragraph>
        <FormattedMessage {...GuidelinesModalMessages.SectionTwoParagraphThree}/>
      </Paragraph>
    </SectionWrapper>
  </Modal>
);

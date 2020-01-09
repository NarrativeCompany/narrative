import * as React from 'react';
import { Col, Modal, Row } from 'antd';
import { Logo } from '../../../shared/components/Logo';
import { FormattedMessage } from 'react-intl';
import { Enable2FAMessages } from '../../../shared/i18n/Enable2FAMessages';
import { printableModalProps } from '../../../shared/utils/modalUtils';
import { AuthHeader } from '../../../shared/styled/shared/auth';
import { Enable2FAModalBody } from './Enable2FAModalBody';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import styled from '../../../shared/styled';
import { Heading, HeadingProps } from '../../../shared/components/Heading';
import { FlexContainer } from '../../../shared/styled/shared/containers';

export interface Enable2FAModalProps {
  dismiss: () => void;
  // jw: this callback is used to control the underlying checkbox indicating if 2FA is enabled.
  success: () => void;
  visible: boolean;
}

const NarrativeContainer = styled<HeadingProps>(Heading)`
  text-transform: uppercase;
  margin: 0 0 0 10px;
  
  display: none;
  @media print {
    display: inline;
  }
`;

/*
  jw: To ensure that the body and everything about this modal gets cleared when it is closed, we need to render the
      entire body as its own component. That should ensure that if the user shorts out, or completes the process that
      the data that drives this modal will be reset.
*/

export const Enable2FAModal: React.SFC<Enable2FAModalProps> = (props) => {
  const { visible, ...rest } = props;

  return (
    <Modal
      visible={visible}
      onCancel={props.dismiss}
      footer={null}
      destroyOnClose={true}
      width={1000}
      {...printableModalProps}
    >
      <Row type="flex" align="middle" justify="center" style={{ paddingBottom: 10 }}>
        <Col>
          <FlexContainer alignItems="center">
            <Logo/>
            <NarrativeContainer size={3}>
              <FormattedMessage {...SharedComponentMessages.Narrative}/>
            </NarrativeContainer>
          </FlexContainer>
        </Col>
      </Row>

      <Row type="flex" align="middle" justify="center">
        <Col>
          <AuthHeader>
            <FormattedMessage {...Enable2FAMessages.PageTitle}/>
          </AuthHeader>
        </Col>
      </Row>

      <Enable2FAModalBody {...rest} />
    </Modal>
  );
};

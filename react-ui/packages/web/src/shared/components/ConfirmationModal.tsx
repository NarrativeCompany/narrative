import * as React from 'react';
import { Modal } from 'antd';
import { FormattedMessage, MessageValue } from 'react-intl';
import { Heading } from './Heading';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { FormButtonGroup } from './FormButtonGroup';
import { ButtonProps } from './Button';
import { AnchorProps } from './Link';
import styled from '../styled';

const BodyWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin: 40px 0 40px;
  
  h2 {
    max-width: 340px;
    margin-bottom: 40px;
  }
`;

export interface ConfirmationModalProps {
  visible: boolean;
  processing?: boolean;
  dismiss: () => void;
  onConfirmation: () => void;
  title: FormattedMessage.MessageDescriptor;
  titleValues?: {[key: string]: MessageValue | JSX.Element};
  btnText: FormattedMessage.MessageDescriptor;
  btnProps: ButtonProps;
  linkText: FormattedMessage.MessageDescriptor;
  linkProps: AnchorProps;
}

export const ConfirmationModal: React.SFC<ConfirmationModalProps> = (props) => {
  const {
    visible,
    processing,
    onConfirmation,
    dismiss,
    title,
    titleValues,
    btnText,
    btnProps,
    linkText,
    linkProps
  } = props;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      footer={null}
      destroyOnClose={true}
      width={500}
    >
      <BodyWrapper column={true} centerAll={true}>
        <Heading size={2} textAlign="center" weight={300}>
          <FormattedMessage {...title} values={titleValues}/>
        </Heading>

        {props.children}

        <FormButtonGroup
          btnText={<FormattedMessage {...btnText}/>}
          btnProps={{
            onClick: onConfirmation,
            loading: processing,
            style: { minWidth: 220 },
            ...btnProps
          }}
          linkText={<FormattedMessage {...linkText}/>}
          linkProps={{ style: { marginTop: 10 }, color: 'light', ...linkProps }}
          direction="column-reverse"
        />
      </BodyWrapper>
    </Modal>
  );
};

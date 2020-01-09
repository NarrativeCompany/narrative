import * as React from 'react';
import { Modal } from 'antd';
import { FlexContainer, FlexContainerProps } from '../styled/shared/containers';
import { Button } from './Button';
import styled from '../styled';
import { ErrorBody, ErrorBodyParentProps } from './ErrorBody';

const ErrorModalContentWrapper = styled<FlexContainerProps>(FlexContainer)`
  @media screen and (max-width: 576px) {
    h1, h3, p {
      text-align: center;
    }
  }
`;

export interface ErrorModalParentProps extends ErrorBodyParentProps {
  visible: boolean;
  // tslint:disable-next-line no-any
  dismiss: () => any;
  btnText: React.ReactNode;
}
export const ErrorModal: React.SFC<ErrorModalParentProps> = (props) => {
  const { visible, dismiss, btnText, ...bodyProps } = props;

  return (
    <Modal
      onCancel={dismiss}
      visible={visible}
      footer={null}
      destroyOnClose={true}
      width={650}
      bodyStyle={{padding: '40px 25px'}}
    >
      <ErrorModalContentWrapper column={true} centerAll={true}>
        <ErrorBody {...bodyProps} />

        <Button
          type="primary"
          size="large"
          style={{minWidth: 180}}
          onClick={dismiss}
        >
          {btnText}
        </Button>
      </ErrorModalContentWrapper>
    </Modal>
  );
};

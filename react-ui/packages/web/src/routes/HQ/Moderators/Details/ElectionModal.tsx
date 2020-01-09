import * as React from 'react';
import { Modal } from 'antd';
import { Heading } from '../../../../shared/components/Heading';
import { FlexContainer, FlexContainerProps } from '../../../../shared/styled/shared/containers';
import styled from '../../../../shared/styled';

const ContentWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin: 0 auto;
  padding: 40px 0 20px;
  
  h3 {
    text-align: center;
    margin-bottom: 40px;
    max-width: 325px;
  }
`;

interface ParentProps {
  visible: boolean;
  dismiss: () => void;
  title: React.ReactNode;
}

export const ElectionModal: React.SFC<ParentProps> = (props) => {
  const { visible, dismiss, title, children } = props;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      destroyOnClose={true}
      footer={null}
    >
      <ContentWrapper column={true} centerAll={true}>
        <Heading size={3} weight={300} style={{lineHeight: '30px'}}>
          {title}
        </Heading>

        {children}
      </ContentWrapper>
    </Modal>
  );
};

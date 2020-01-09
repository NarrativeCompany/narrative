import * as React from 'react';
import { compose } from 'recompose';
import { Layout } from 'antd';
import { LayoutProps } from 'antd/lib/layout';
import { LoginModal } from '../../routes/Login/LoginModal';
import { ModalConnect, ModalName, ModalStoreProps } from '../../shared/stores/ModalStore';
import styled from '../../shared/styled';

interface ParentProps {
  children: React.ReactNode;
}

type Props =
  ParentProps &
  LayoutProps &
  ModalStoreProps;

const ContentWrapper = styled<LayoutProps>(Layout.Content)`
  width: 100%;
`;

const ContentComponent: React.SFC<Props> = (props) => {
  const { children, modalStoreValues, modalStoreActions, ...contentProps } = props;

  return (
    <ContentWrapper {...contentProps}>
      {children}

      <LoginModal
        dismiss={() => modalStoreActions.updateModalVisibility(ModalName.login)}
        visible={modalStoreValues.isVisible}
      />
    </ContentWrapper>
  );
};

export const Content = compose(
  ModalConnect(ModalName.login)
)(ContentComponent) as React.ComponentClass<{}>;

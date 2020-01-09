import * as React from 'react';
import { Modal } from 'antd';
import { compose, withHandlers } from 'recompose';
import { InjectedIntlProps, injectIntl } from 'react-intl';
import { Form, FormikProps, withFormik } from 'formik';
import {
  MethodError,
  initialFormState,
  applyExceptionToState,
  deleteUserFormFormikUtil,
  DeleteUserFormValues,
  WithCurrentUserTwoFactorAuthStateProps,
  withDeleteCurrentUser,
  WithDeleteCurrentUserProps,
  withState,
  WithStateProps,
  TwoFactorEnabledOnAccountProps
} from '@narrative/shared';
import { AuthWrapper } from '../../../../shared/styled/shared/auth';
import styled from '../../../../shared/styled';
import { WebRoute } from '../../../../shared/constants/routes';
import { RouterProps, withRouter } from 'react-router';
import { logout } from '../../../../shared/utils/authTokenUtils';
import { DeleteAccountSuccessPanel } from './DeleteAccountSuccessPanel';
import { DeleteAccountAckCBPanel } from './DeleteAccountAckCBPanel';
import { DeleteAccountFinalAckPanel } from './DeleteAccountFinalAckPanel';
import { withCurrentUserTwoFactorAuthEnabled } from '../../../../shared/containers/withCurrentUserTwoFactorAuthEnabled';

enum Page {
  CHECKBOXES,
  FINAL_ACKNOWLEDGE,
  GOODBYE
}

interface ParentProps {
  // tslint:disable-next-line no-any
  dismiss: () => any;
  visible: boolean;
}

type State =  MethodError & {
  curPage: Page;
  isSubmitting?: boolean;
};
const initialState: State = {
  ...initialFormState,
  curPage: Page.CHECKBOXES
};

interface WithHandlers {
  handleAdvancePage: () => void;
  handleDismiss: () => void;
  handleGoodbye: () => void;
}

type Props =
  TwoFactorEnabledOnAccountProps &
  WithStateProps<State> &
  ParentProps &
  WithHandlers &
  FormikProps<DeleteUserFormValues> &
  RouterProps &
  InjectedIntlProps &
  WithCurrentUserTwoFactorAuthStateProps &
  WithDeleteCurrentUserProps;

export const ModalForm = styled(Form)`
  width: 100%;
`;

const DeleteAccountModalComponent: React.SFC<Props> = (props) => {
  const { visible, handleAdvancePage, handleDismiss, handleGoodbye, twoFactorEnabled } = props;
  const { methodError, curPage, isSubmitting } = props.state;

  return (
    <Modal
      visible={visible}
      onCancel={handleDismiss}
      closable={false}
      footer={null}
      destroyOnClose={true}
      width={600}
    >

      <AuthWrapper>
        <ModalForm>

          {curPage === Page.CHECKBOXES &&
          <DeleteAccountAckCBPanel
            methodError={methodError}
            handleContinue={handleAdvancePage}
            handleDismiss={handleDismiss}
          />}

          {curPage === Page.FINAL_ACKNOWLEDGE &&
          <DeleteAccountFinalAckPanel
            methodError={methodError}
            handleDismiss={handleDismiss}
            loading={isSubmitting}
            show2FAInput={twoFactorEnabled}
          />}

          {curPage === Page.GOODBYE &&
          <DeleteAccountSuccessPanel
            handleDismiss={handleGoodbye}
          />}

        </ModalForm>
      </AuthWrapper>

    </Modal>
  );
};

export const DeleteAccountModal = compose(
  injectIntl,
  withRouter,
  withCurrentUserTwoFactorAuthEnabled,
  withState<State>(initialState),
  withHandlers({
    handleAdvancePage: (props: Props) => async () => {
      const { state: {curPage}, setState } = props;
      const nextPage: Page = Page[Page[curPage + 1]];
      setState(ss => ({...ss, curPage: nextPage}));
    },
    handleDismiss:  (props: Props) => async () => {
      props.setState(initialState);
      props.dismiss();
    },
    handleGoodbye:  (props: Props) => async () => {
      props.setState(initialState);
      props.dismiss();
      await logout();
      await props.history.push(WebRoute.Home);
    }
  }),
  withDeleteCurrentUser,
  withFormik<Props, DeleteUserFormValues>({
    ...deleteUserFormFormikUtil,
    handleSubmit: async (values, {props, setErrors}) => {
      const input = {...values, confirmDeleteAccount: true};
      const { setState, deleteCurrentUser, state: {curPage}, isSubmitting } = props;

      if (Page.FINAL_ACKNOWLEDGE !== curPage || isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        await deleteCurrentUser({input});
        setState(ss => ({...ss, curPage: Page.GOODBYE}));

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        setState(ss => ({...ss, methodError: null, isSubmitting: undefined}));
      }
    },
  })
)(DeleteAccountModalComponent) as React.ComponentClass<ParentProps>;

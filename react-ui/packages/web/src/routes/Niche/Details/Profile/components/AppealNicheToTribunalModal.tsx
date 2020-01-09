import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Form, FormikProps, withFormik } from 'formik';
import { Modal } from 'antd';
import { Heading } from '../../../../../shared/components/Heading';
import { Paragraph } from '../../../../../shared/components/Paragraph';
import { FormField } from '../../../../../shared/components/FormField';
import { FormButtonGroup } from '../../../../../shared/components/FormButtonGroup';
import { FormMethodError } from '../../../../../shared/components/FormMethodError';
import { FormattedMessage } from 'react-intl';
import { AppealNicheToTribunalModalMessages } from '../../../../../shared/i18n/AppealNicheToTribunalModalMessages';
import { SharedComponentMessages } from '../../../../../shared/i18n/SharedComponentMessages';
import { getAvailableTribunalIssueTypeByType } from '../../../../../shared/utils/tribunalIssuesUtil';
import {
  appealToTribunalFormikUtil,
  AppealToTribunalFormValues,
  applyExceptionToState,
  CreateNicheTribunalIssueInput,
  initialFormState,
  MethodError,
  NicheDetail,
  TribunalIssueType,
  withCreateNicheTribunalIssue,
  WithCreateNicheTribunalIssueProps,
  withState,
  WithStateProps
} from '@narrative/shared';

interface WithProps {
  isNicheStatusRejected: boolean;
}

// tslint:disable no-any
export interface AppealNicheToTribunalModalProps {
  visible?: boolean;
  dismiss: () => any;
  onSubmitSuccess: (tribunalIssueOid: string) => any;
  nicheDetail: NicheDetail;
}
// tslint:enable no-any

type Props =
  AppealNicheToTribunalModalProps &
  WithStateProps<MethodError> &
  WithProps &
  FormikProps<AppealToTribunalFormValues> &
  WithCreateNicheTribunalIssueProps;

const AppealNicheToTribunalModalComponent: React.SFC<Props> = (props) => {
  const { visible, dismiss, isNicheStatusRejected, state, isSubmitting } = props;

  const descriptionMessage = isNicheStatusRejected ?
    AppealNicheToTribunalModalMessages.DescriptionRejected :
    AppealNicheToTribunalModalMessages.DescriptionApproved;

  return (
    <Modal
      visible={visible}
      onCancel={dismiss}
      footer={null}
      destroyOnClose={true}
      width={600}
    >
      <Form>
        <Heading size={3}>
          <FormattedMessage {...AppealNicheToTribunalModalMessages.Title}/>
        </Heading>

        <Paragraph color="light" marginBottom="large">
          <FormattedMessage {...descriptionMessage}/>
        </Paragraph>

        <FormMethodError methodError={state.methodError}/>

        <FormField.TextArea
          name="comment"
          label={<FormattedMessage {...AppealNicheToTribunalModalMessages.CommentFieldLabel}/>}
          rows={4}
        />

        <FormButtonGroup
          btnText={<FormattedMessage {...SharedComponentMessages.SubmitBtnText}/>}
          linkText={<FormattedMessage {...SharedComponentMessages.Cancel}/>}
          btnProps={{htmlType: 'submit', loading: isSubmitting}}
          linkProps={{onClick: dismiss}}
        />
      </Form>
    </Modal>
  );
};

export const AppealNicheToTribunalModal = compose(
  withCreateNicheTribunalIssue,
  withState<MethodError>(initialFormState),
  withProps((props: Props) => {
    const { nicheDetail: { availableTribunalIssueTypes } } = props;

    const isNicheStatusRejected = !!getAvailableTribunalIssueTypeByType(
      availableTribunalIssueTypes,
      TribunalIssueType.APPROVE_REJECTED_NICHE
    );

    return { isNicheStatusRejected };
  }),
  withFormik<Props, AppealToTribunalFormValues>({
    ...appealToTribunalFormikUtil,
    handleSubmit: async (values, { props, setErrors, setSubmitting }) => {
      const {
        setState,
        isNicheStatusRejected,
        createNicheTribunalIssue,
        nicheDetail: { niche },
        onSubmitSuccess,
        isSubmitting
      } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        const type = isNicheStatusRejected ?
          TribunalIssueType.APPROVE_REJECTED_NICHE :
          TribunalIssueType.RATIFY_NICHE;
        const input: CreateNicheTribunalIssueInput = { type, ...values };

        const tribunalIssueDetail = await createNicheTribunalIssue(input, niche.oid);
        onSubmitSuccess(tribunalIssueDetail.tribunalIssue.oid);
      } catch (e) {
        applyExceptionToState(e, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(AppealNicheToTribunalModalComponent) as React.ComponentClass<AppealNicheToTribunalModalProps>;

import * as React from 'react';
import { compose } from 'recompose';
import { Form, FormikProps, withFormik } from 'formik';
import { Modal } from 'antd';
import { Heading } from '../../../../../../shared/components/Heading';
import { Paragraph } from '../../../../../../shared/components/Paragraph';
import { FormField } from '../../../../../../shared/components/FormField';
import { FormButtonGroup } from '../../../../../../shared/components/FormButtonGroup';
import { FormMethodError } from '../../../../../../shared/components/FormMethodError';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../../../../../../shared/i18n/SharedComponentMessages';
import {
  appealToTribunalFormikUtil,
  AppealToTribunalFormValues,
  applyExceptionToState,
  CreatePublicationTribunalIssueInput,
  initialFormState,
  MethodError,
  PublicationDetail,
  withCreatePublicationTribunalIssue,
  WithCreatePublicationTribunalIssueProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import {
  AppealPublicationToTribunalModalMessages
} from '../../../../../../shared/i18n/AppealPublicationToTribunalModalMessages';

// tslint:disable no-any
export interface AppealPublicationToTribunalModalProps {
  visible?: boolean;
  dismiss: () => any;
  onSubmitSuccess: (tribunalIssueOid: string) => any;
  publicationDetail: PublicationDetail;
}
// tslint:enable no-any

type Props =
  AppealPublicationToTribunalModalProps &
  WithStateProps<MethodError> &
  FormikProps<AppealToTribunalFormValues> &
  WithCreatePublicationTribunalIssueProps;

const AppealPublicationToTribunalModalComponent: React.SFC<Props> = (props) => {
  const { visible, dismiss, state, isSubmitting } = props;

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
          <FormattedMessage {...AppealPublicationToTribunalModalMessages.Title}/>
        </Heading>

        <Paragraph color="light" marginBottom="large">
          <FormattedMessage {...AppealPublicationToTribunalModalMessages.Description}/>
        </Paragraph>

        <FormMethodError methodError={state.methodError}/>

        <FormField.TextArea
          name="comment"
          label={<FormattedMessage {...AppealPublicationToTribunalModalMessages.CommentFieldLabel}/>}
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

export const AppealPublicationToTribunalModal = compose(
  withCreatePublicationTribunalIssue,
  withState<MethodError>(initialFormState),
  withFormik<Props, AppealToTribunalFormValues>({
    ...appealToTribunalFormikUtil,
    handleSubmit: async (values, { props, setErrors, setSubmitting }) => {
      const {
        setState,
        createPublicationTribunalIssue,
        publicationDetail: { publication },
        onSubmitSuccess,
        isSubmitting
      } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        const input: CreatePublicationTribunalIssueInput = { ...values };

        const tribunalIssueDetail = await createPublicationTribunalIssue(input, publication.oid);
        onSubmitSuccess(tribunalIssueDetail.tribunalIssue.oid);
      } catch (e) {
        applyExceptionToState(e, setErrors, setState);
      }

      setSubmitting(false);
    }
  })
)(AppealPublicationToTribunalModalComponent) as React.ComponentClass<AppealPublicationToTribunalModalProps>;

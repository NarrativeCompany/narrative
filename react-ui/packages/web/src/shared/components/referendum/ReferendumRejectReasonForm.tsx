import * as React from 'react';
import { compose, withProps } from 'recompose';
import { Form as FormikForm, FormikProps, withFormik } from 'formik';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { RadioField, RadioGroupField } from '../RadioGroupField';
import { FlexContainer, FlexContainerProps } from '../../styled/shared/containers';
import { Button } from '../Button';
import { FormField } from '../FormField';
import { ApprovalCardBackMessages } from '../../i18n/ApprovalCardBackMessages';
import { SharedComponentMessages } from '../../i18n/SharedComponentMessages';
import { EnhancedReferendumVoteReason } from '../../enhancedEnums/referendumVoteReason';
import { Link } from '../Link';
import { showValidationErrorDialogIfNecessary } from '../../utils/webErrorUtils';
import {
  Referendum,
  referendumVoteFormikUtil,
  ReferendumVoteFormValues,
  ReferendumVoteReason,
  VoteOnReferendumInput,
  withVoteOnReferendum,
  WithVoteOnReferendumProps,
  MethodError,
  initialFormState,
  withState,
  WithStateProps
} from '@narrative/shared';
import styled from '../../../shared/styled';
import { ReferendumMessages } from '../../i18n/ReferendumMessages';

const Form = styled(FormikForm)`
  width: 100%;
  height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
`;

const ButtonWrapper = styled.div`
  width: 150px;
  margin: auto 0 5px;
`;

const FormBottomWrapper = styled<FlexContainerProps>(FlexContainer)`
  width: 100%;
  margin-top: 10px;
`;

interface WithProps {
  reason?: ReferendumVoteReason;
}

// tslint:disable no-any
interface ParentProps {
  referendum: Referendum;
  dismissForm: () => any;
}
// tslint:enable no-any

type WithFormikProps =
  ParentProps &
  FormikProps<ReferendumVoteFormValues> &
  WithVoteOnReferendumProps &
  WithProps &
  InjectedIntlProps &
  WithStateProps<MethodError>;

type Props =
  ParentProps &
  InjectedIntlProps &
  WithProps &
  ParentProps &
  FormikProps<ReferendumVoteFormValues> &
  WithVoteOnReferendumProps;

const ReferendumRejectReasonFormComponent: React.SFC<Props> = (props) => {
  const { dismissForm, intl, isSubmitting } = props;

  const radioFields: RadioField = EnhancedReferendumVoteReason.enhancers.map((enhancedReason) => {
    let message;
    if (enhancedReason.reason === ReferendumVoteReason.VIOLATES_TOS) {
      const termsOfService = (
        <Link.Legal type="tos">
          <FormattedMessage {...ReferendumMessages.TOS}/>
        </Link.Legal>
      );
      const acceptableUsePolicy = (
        <Link.Legal type="aup">
          <FormattedMessage {...ReferendumMessages.AUP}/>
        </Link.Legal>
      );
      message = <FormattedMessage {...enhancedReason.radioMessage} values={{termsOfService, acceptableUsePolicy}}/>;
    } else {
      message = <FormattedMessage {...enhancedReason.radioMessage}/>;
    }
    return {
      value: enhancedReason.reason,
      text: message
    };
  });

  return (
    <Form>
      <RadioGroupField
        name="reason"
        fieldMargin={0}
        radioFields={radioFields}
      />

      <FormField.Input
        name="comment"
        style={{width: '100%', marginBottom: 0}}
        placeholder={intl.formatMessage(ApprovalCardBackMessages.TextAreaPlaceholder)}
      />

      <FormBottomWrapper justifyContent="space-between" alignItems="center">
        <span onClick={dismissForm} style={{cursor: 'pointer'}}>
          <FormattedMessage {...SharedComponentMessages.Cancel}/>
        </span>

        <ButtonWrapper>
          <Button type="danger" block={true} htmlType="submit" loading={isSubmitting}>
            <FormattedMessage {...ApprovalCardBackMessages.BtnText}/>
          </Button>
        </ButtonWrapper>
      </FormBottomWrapper>
    </Form>
  );
};

export const ReferendumRejectReasonForm = compose(
  injectIntl,
  withState<MethodError>(initialFormState),
  withProps((props: ParentProps) => ({
    reason: props.referendum.currentUserVote &&
      props.referendum.currentUserVote.reason
  })),
  withVoteOnReferendum,
  withFormik<WithFormikProps, ReferendumVoteFormValues>({
    validationSchema: referendumVoteFormikUtil.validationSchema,
    mapPropsToValues: (props: WithProps) => referendumVoteFormikUtil.mapPropsToValues({
      reason: props.reason
    }),
    handleSubmit: async (values, {props, setSubmitting}) => {
      const { voteOnReferendum, referendum, intl, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      const input: VoteOnReferendumInput = {
        votedFor: false,
        reason: values.reason as ReferendumVoteReason,
        comment: values.comment
      };

      try {
        await voteOnReferendum(input, referendum.oid);
      } catch (err) {
        showValidationErrorDialogIfNecessary(intl.formatMessage(SharedComponentMessages.FormErrorTitle), err);
      }

      setSubmitting(false);
      props.dismissForm();
    }
  })
)(ReferendumRejectReasonFormComponent) as React.ComponentClass<ParentProps>;

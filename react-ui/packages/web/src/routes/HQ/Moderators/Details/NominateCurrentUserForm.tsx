import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { Form, withFormik, FormikProps } from 'formik';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { FormField } from '../../../../shared/components/FormField';
import { FormButtonGroup } from '../../../../shared/components/FormButtonGroup';
import { FormMethodError } from '../../../../shared/components/FormMethodError';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';
import {
  applyExceptionToState,
  withState,
  WithStateProps,
  nominateCurrentUserFormikUtil,
  NominateCurrentUserFormValues,
  withNominateCurrentUser,
  WithNominateCurrentUserProps,
  NominateCurrentUserInput,
  MethodError,
  initialFormState
} from '@narrative/shared';

const { TextArea, Input } = FormField;

type State = MethodError & {
  charCount: number;
};
const initialState: State = {
  charCount: 140,
  ...initialFormState
};

interface WithHandlers {
  handleUpdateCharCount: (e: React.SyntheticEvent<HTMLTextAreaElement>) => void;
}

interface ParentProps {
  electionOid: string;
  dismiss?: () => void;
  isCardForm?: boolean;
  personalStatement?: string;
}

type Props =
  ParentProps &
  WithStateProps<State> &
  WithHandlers &
  FormikProps<NominateCurrentUserFormValues> &
  WithNominateCurrentUserProps &
  InjectedIntlProps;

export const NominateCurrentUserFormComponent: React.SFC<Props> = (props) => {
  const { state, handleUpdateCharCount, isSubmitting, isCardForm, dismiss, intl: { formatMessage } } = props;

  const extra = (
    <span style={{float: 'right'}}>
      <FormattedMessage
        {...ModeratorElectionDetailsMessages.CharactersRemaining}
        values={{charCount: state.charCount}}
      />
    </span>
  );

  const placeHolder = formatMessage(ModeratorElectionDetailsMessages.PersonalStatementPlaceholder);

  const FormContent = isCardForm ? (
    <React.Fragment>
      <Input
        name="personalStatement"
        maxLength={140}
        style={{margin: ' 0 0 5px'}}
      />

      <FormButtonGroup
        btnText={<FormattedMessage {...SharedComponentMessages.SaveBtnText}/>}
        linkText={<FormattedMessage {...SharedComponentMessages.Cancel}/>}
        btnProps={{htmlType: 'submit', loading: isSubmitting}}
        linkProps={{onClick: dismiss}}
      />
    </React.Fragment>
  ) : (
    <React.Fragment>
      <TextArea
        name="personalStatement"
        style={{maxWidth: 425, margin: '0 auto 25px'}}
        rows={3}
        maxLength={140}
        placeholder={placeHolder}
        onKeyUp={(e: React.SyntheticEvent<HTMLTextAreaElement>) => handleUpdateCharCount(e)}
        extra={extra}
      />

      <FormButtonGroup
        btnText={<FormattedMessage {...ModeratorElectionDetailsMessages.NominateMyselfBtn}/>}
        linkText={<FormattedMessage {...SharedComponentMessages.Cancel}/>}
        direction="column-reverse"
        btnProps={{htmlType: 'submit', loading: isSubmitting}}
        linkProps={{onClick: dismiss}}
      />
    </React.Fragment>
  );

  return (
    <Form style={{width: '100%'}}>
      <FormMethodError methodError={state.methodError}/>

      {FormContent}
    </Form>
  );
};

export const NominateCurrentUserForm = compose(
  injectIntl,
  withState<State>(initialState),
  withNominateCurrentUser,
  withFormik<Props, NominateCurrentUserFormValues>({
    ...nominateCurrentUserFormikUtil,
    mapPropsToValues: (props: ParentProps) => ({ personalStatement: props.personalStatement || undefined }),
    handleSubmit: async (values, { props, setErrors, setSubmitting }) => {
      const { setState, isSubmitting, nominateCurrentUser, electionOid, dismiss } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({ ...ss, methodError: null }));

      try {
        const input: NominateCurrentUserInput = { ...values };
        await nominateCurrentUser(input, electionOid);

        if (dismiss) {
          dismiss();
        }
      } catch (e) {
        applyExceptionToState(e, setErrors, setState);
        setSubmitting(false);
      }
    }
  }),
  withHandlers({
    handleUpdateCharCount: (props: WithStateProps<State>) => (e: React.SyntheticEvent<HTMLTextAreaElement>) => {
      const { setState } = props;

      const charLength =
        e.currentTarget &&
        e.currentTarget.value &&
        e.currentTarget.value.length;

      const charCount = initialState.charCount - (charLength || 0);
      setState(ss => ({ ...ss, charCount }));
    }
  })
)(NominateCurrentUserFormComponent) as React.ComponentClass<ParentProps>;

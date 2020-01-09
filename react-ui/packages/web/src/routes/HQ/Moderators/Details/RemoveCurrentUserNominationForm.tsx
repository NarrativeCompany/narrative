import * as React from 'react';
import { compose } from 'recompose';
import { Form, withFormik, FormikProps } from 'formik';
import { FormattedMessage } from 'react-intl';
import { FormButtonGroup } from '../../../../shared/components/FormButtonGroup';
import { FormMethodError } from '../../../../shared/components/FormMethodError';
import { ModeratorElectionDetailsMessages } from '../../../../shared/i18n/ModeratorElectionDetailsMessages';
import { SharedComponentMessages } from '../../../../shared/i18n/SharedComponentMessages';
import {
  applyExceptionToState,
  withRemoveCurrentUserNominee,
  WithRemoveCurrentUserNominationProps,
  withState,
  WithStateProps,
  MethodError,
  initialFormState
} from '@narrative/shared';

interface ParentProps {
  dismiss: () => void;
  electionOid: string;
}

type Props =
  ParentProps &
  WithRemoveCurrentUserNominationProps &
  FormikProps<{}> &
  WithStateProps<MethodError>;

const RemoveCurrentUserNominationFormComponent: React.SFC<Props> = (props) => {
  const { dismiss, state } = props;

  return (
    <Form>
      <FormMethodError methodError={state.methodError}/>

      <FormButtonGroup
        btnText={<FormattedMessage {...ModeratorElectionDetailsMessages.RevokeMyNominationFormBtnText}/>}
        linkText={<FormattedMessage {...SharedComponentMessages.Cancel}/>}
        direction="column-reverse"
        btnProps={{type: 'danger', htmlType: 'submit'}}
        linkProps={{onClick: dismiss}}
      />
    </Form>
  );
};

export const RemoveCurrentUserNominationForm = compose(
  withState<MethodError>(initialFormState),
  withRemoveCurrentUserNominee,
  withFormik<Props, {}>({
    handleSubmit: async (_, { props, setErrors, setSubmitting }) => {
      const { setState, isSubmitting, removeCurrentUserNomination, electionOid, dismiss } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({ ...ss, methodError: null }));

      try {
        await removeCurrentUserNomination(electionOid);

        if (dismiss) {
          dismiss();
        }
      } catch (e) {
        applyExceptionToState(e, setErrors, setState);
        setSubmitting(false);
      }
    }
  })
)(RemoveCurrentUserNominationFormComponent) as React.ComponentClass<ParentProps>;

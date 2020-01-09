import * as React from 'react';
import { compose } from 'recompose';
import { ChangePublicationOwnerModalProps } from './ChangePublicationOwnerModal';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import {
  withCurrentUserTwoFactorAuthEnabled
} from '../../../../../shared/containers/withCurrentUserTwoFactorAuthEnabled';
import { FormikProps, withFormik } from 'formik';
import { openNotification } from '../../../../../shared/utils/notificationsUtil';
import {
  withState,
  WithStateProps,
  SimpleFormState,
  initialFormState,
  withChangePublicationOwner,
  WithChangePublicationOwnerProps,
  ChangePublicationOwnerFormValues,
  changePublicationOwnerFormFormikUtil,
  TwoFactorEnabledOnAccountProps,
  applyExceptionToState
} from '@narrative/shared';
import {
  AccountVerificationFormWrapper,
  distanceBetweenAccountVerificationFields
} from '../../../../../shared/components/AccountVerificationFormWrapper';
import { FormMethodError } from '../../../../../shared/components/FormMethodError';
import { SelectField, SelectFieldValues } from '../../../../../shared/components/SelectField';
import { PublicationDetailsMessages } from '../../../../../shared/i18n/PublicationDetailsMessages';
import { handlePowerUserChangeForCurrentUser } from '../../../../../shared/utils/publicationUtils';
import { withRouter, RouteComponentProps } from 'react-router';
import { FormControl } from '../../../../../shared/components/FormControl';

type ParentProps = Pick<ChangePublicationOwnerModalProps, 'publication' | 'potentialOwners' | 'close'>;

type Props = ParentProps &
  TwoFactorEnabledOnAccountProps &
  FormikProps<ChangePublicationOwnerFormValues> &
  WithStateProps<SimpleFormState> &
  InjectedIntlProps &
  RouteComponentProps;

const ChangePublicationOwnerFormComponent: React.SFC<Props> = (props) => {
  const { potentialOwners, twoFactorEnabled, state, isSubmitting, intl: { formatMessage } } = props;

  const selectFields: SelectFieldValues[] = potentialOwners.reduce((fields, admin)  => {
    const value = admin.oid;
    const text = admin.displayName;
    fields.push({value, text});

    return fields;
  }, [] as SelectFieldValues[]);

  return (
    <AccountVerificationFormWrapper
      title={PublicationDetailsMessages.ChangePublicationOwnerFormTitle}
      description={<FormattedMessage {...PublicationDetailsMessages.ChangePublicationOwnerFormDescription}/>}
      submitText={PublicationDetailsMessages.ChangePublicationOwnerFormButtonText}
      isSubmitting={isSubmitting}
      twoFactorEnabled={twoFactorEnabled}
    >

      <FormMethodError methodError={state.methodError} />

      <FormControl style={{ marginBottom: distanceBetweenAccountVerificationFields }}>
        <SelectField
          name="userOid"
          label={<FormattedMessage {...PublicationDetailsMessages.NewPublicationOwnerSelectorLabel}/>}
          selectFields={selectFields}
          placeholder={formatMessage(PublicationDetailsMessages.NewPublicationOwnerSelectorPlaceholder)}
          style={{ width: '100%', marginBottom: 0 }}
        />
      </FormControl>

    </AccountVerificationFormWrapper>
  );
};

export const ChangePublicationOwnerForm = compose(
  injectIntl,
  withCurrentUserTwoFactorAuthEnabled,
  withState<SimpleFormState>(initialFormState),
  withChangePublicationOwner,
  withRouter,
  withFormik<Props & WithChangePublicationOwnerProps, ChangePublicationOwnerFormValues>({
    ...changePublicationOwnerFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const {
        setState,
        close,
        isSubmitting,
        history,
        potentialOwners,
        intl: { formatMessage },
        changePublicationOwner,
        publication: { oid }
      } = props;

      const { userOid } = values;
      const user = potentialOwners.find(potentialOwner => potentialOwner.oid === userOid);
      if (!userOid || !user) {
        // todo:error-reporting: In order for the placeholder text to appear in the selector this field needs to be
        //      undefined to begin with. We have a validator to ensure that a value is selected, so this should never
        //      come up. As a result, we should log to the server if it does.
        return;
      }

      // jw: now that we have proven to the compiler above that we have a userOid, let's define input so that userOid
      //     is not optional.
      const input = {...values, userOid};
      const newOwnerName = user.displayName;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      let redirected;
      try {

        const result = await changePublicationOwner(input, oid);

        if (result) {
          // jw: now that we have changed the owner, let's ensure that the current user still has access to this page.
          redirected = await handlePowerUserChangeForCurrentUser(
            false,
            result.publicationDetail,
            history,
            formatMessage,
            PublicationDetailsMessages.NoLongerHavePowerUserAccessDueToOwnerChange,
            {newOwnerName}
          );
          // jw: if the above code redirected the user then let's short out and prevent the state change
          if (redirected) {
            return;
          }

          // jw: since we are staying here, let's close the modal and give a message.
          close();

          // Notify the user of success
          await openNotification.updateSuccess(
            {
              message: formatMessage(PublicationDetailsMessages.OwnerChanged),
              description: formatMessage(PublicationDetailsMessages.OwnerChangedDescription, {newOwnerName}),
              duration: 5
            });
        }

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        // jw: don't update state if we redirected as part of success processing.
        if (!redirected) {
          setSubmitting(false);
          setState(ss => ({...ss, methodError: null, isSubmitting: undefined}));
        }
      }
    }
  })
)(ChangePublicationOwnerFormComponent) as React.ComponentClass<ParentProps>;

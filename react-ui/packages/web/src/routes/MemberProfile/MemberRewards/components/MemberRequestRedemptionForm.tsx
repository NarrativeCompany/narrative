import * as React from 'react';
import { compose } from 'recompose';
import { FormikProps, withFormik } from 'formik';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { FormMethodError } from '../../../../shared/components/FormMethodError';
import { withCurrentUserTwoFactorAuthEnabled } from '../../../../shared/containers/withCurrentUserTwoFactorAuthEnabled';
import {
  applyExceptionToState,
  initialFormState,
  MethodError,
  requestRedemptionFormFormikUtil,
  RequestRedemptionFormValues,
  omitProperties,
  TwoFactorEnabledOnAccountProps,
  withNrveUsdPrice,
  WithNrveUsdPriceProps,
  withState,
  WithStateProps,
  withRequestRedemption,
  WithRequestRedemptionProps,
  RequestRedemptionInput,
  UserNeoWallet,
  NrveUsdPriceInput
} from '@narrative/shared';
import { openNotification } from '../../../../shared/utils/notificationsUtil';
import { withLoadingPlaceholder } from '../../../../shared/utils/withLoadingPlaceholder';
import { MemberRewardsMessages } from '../../../../shared/i18n/MemberRewardsMessages';
import {
  AccountVerificationFormWrapper,
  distanceBetweenAccountVerificationFields
} from '../../../../shared/components/AccountVerificationFormWrapper';
import { Block } from '../../../../shared/components/Block';
import { NrveValue } from '../../../../shared/components/rewards/NrveValue';
import { NrveInput } from '../../../../shared/components/NrveInput';
import { Link } from '../../../../shared/components/Link';
import { WebRoute } from '../../../../shared/constants/routes';

interface State extends MethodError {
  isSubmitting?: boolean;
}

interface ParentProps {
  onRedemptionRequested: () => void;
  userNeoWallet: UserNeoWallet;
}

type Props = ParentProps &
  TwoFactorEnabledOnAccountProps &
  FormikProps<RequestRedemptionFormValues> &
  WithStateProps<State> &
  WithNrveUsdPriceProps &
  InjectedIntlProps;

const MemberRequestRedemptionFormComponent: React.SFC<Props> = (props) => {
  const {
    twoFactorEnabled,
    state,
    isSubmitting,
    userNeoWallet,
    errors,
    nrveUsdPrice,
    values,
    intl: { formatMessage }
  } = props;

  const currentBalance = <NrveValue value={userNeoWallet.currentBalance} showFullDecimal={true} />;

  const footer = (
    <Block size="small" color="lightGray">
      <FormattedMessage {...MemberRewardsMessages.UsdEstimateDisclaimer} />
    </Block>
  );

  const nrveLink = <Link.About type="nrve" target="_blank" />;
  const neoWalletLink = (
    <Link to={WebRoute.MemberNeoWallet} target="_blank">
      <FormattedMessage {...MemberRewardsMessages.RequestRedemptionDescriptionNeoWalletLink} />
    </Link>
  );

  return (
    <AccountVerificationFormWrapper
      title={MemberRewardsMessages.RequestRedemption}
      description={
        <FormattedMessage
          {...MemberRewardsMessages.RequestRedemptionDescription}
          values={{nrveLink, neoWalletLink}}
        />
      }
      submitText={MemberRewardsMessages.SubmitRequestBtnText}
      isSubmitting={isSubmitting}
      twoFactorEnabled={twoFactorEnabled}
      footer={footer}
    >

      <Block color="success" style={{ fontSize: 18 }}>
        <FormattedMessage {...MemberRewardsMessages.CurrentBalance} values={{currentBalance}}/>
      </Block>

      <FormMethodError methodError={state.methodError} />

      <NrveInput
        placeholder={formatMessage(MemberRewardsMessages.RedemptionAmount)}
        name="redemptionAmount"
        style={{marginBottom: distanceBetweenAccountVerificationFields}}
        autoFocus={true}
        errorCanContainHtml={true}
        errors={errors}
        values={values}
        nrveUsdPrice={nrveUsdPrice.nrveUsdPrice}
      />

    </AccountVerificationFormWrapper>
  );
};

export const MemberRequestRedemptionForm = compose(
  withNrveUsdPrice,
  withLoadingPlaceholder(),
  injectIntl,
  withCurrentUserTwoFactorAuthEnabled,
  withState<State>(initialFormState),
  withRequestRedemption,
  withFormik<Props & WithRequestRedemptionProps, RequestRedemptionFormValues>({
    ...requestRedemptionFormFormikUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, onRedemptionRequested, isSubmitting, intl: { formatMessage } } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null, isSubmitting: true}));

      try {
        const nrveUsdPrice = omitProperties(props.nrveUsdPrice, ['__typename']) as NrveUsdPriceInput;

        const input: RequestRedemptionInput = { ...values, nrveUsdPrice };
        await props.requestRedemption(input);

        // jw: allow the caller to control what happens when the user requests a redemption.
        onRedemptionRequested();

        // Notify the user of success
        await openNotification.updateSuccess(
          {
            message: formatMessage(MemberRewardsMessages.RedemptionRequestedTitle),
            description: formatMessage(MemberRewardsMessages.RedemptionRequestedDescription),
            duration: null
          });

      } catch (exception) {
        applyExceptionToState(exception, setErrors, setState);

      } finally {
        setSubmitting(false);
        setState(ss => ({...ss, methodError: null, isSubmitting: undefined}));
      }

    },
  }),
)(MemberRequestRedemptionFormComponent) as React.ComponentClass<ParentProps>;

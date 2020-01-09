import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import { Form, FormikProps, withFormik } from 'formik';
import { FormattedMessage, InjectedIntlProps, injectIntl } from 'react-intl';
import { Select } from 'antd';
import { SelectValue } from 'antd/lib/select';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { FormControl } from '../../../shared/components/FormControl';
import { Button } from '../../../shared/components/Button';
import { themeTypography } from '../../../shared/styled/theme';
import { FormControlWrapper } from './FormControlWrapper';
import { DragAndDropField } from '../../../shared/components/DragAndDrop';
import { SelfieExample } from './SelfieExample';
import { FormMethodError } from '../../../shared/components/FormMethodError';
import { RcFile } from 'antd/lib/upload/interface';
import { MemberCertificationFormMessages } from '../../../shared/i18n/MemberCertificationFormMessages';
import { SharedComponentMessages } from '../../../shared/i18n/SharedComponentMessages';
import { WebRoute } from '../../../shared/constants/routes';
import {
  applyExceptionToState,
  buildBodySerializerForKycApplicantMutation,
  initialFormState,
  MethodError,
  kycApplicantFormikUtil,
  KycApplicantFormValues,
  KycIdentificationType,
  withState,
  WithStateProps,
  withSubmitKycApplicant,
  WithSubmitKycApplicantProps,
  KycApplicationInput,
  fileSizeMax,
  DOC_FRONT_IMAGE,
  DOC_BACK_IMAGE,
  LIVE_PHOTO_IMAGE,
  KYC_IDENTIFICATION_TYPE
} from '@narrative/shared';
import styled from '../../../shared/styled';

const UploadWrapper = styled<FlexContainerProps>(FlexContainer)`
  > div:first-child {
    margin-right: 20px;
  }

  @media screen and (max-width: 767px) {
    flex-direction: column;
  
    > div:first-child {
      margin-right: 0;
      margin-bottom: 20px;
    }
  }
`;

const selectColProps = {
  wrapperCol: {
    sm: 24,
    md: 16,
    lg: 14,
    xl: 12
  }
};
const sharedAcceptedFileTypes = '.jpg,.jpeg,.png';
export const idTypeOptions = [
  { message: MemberCertificationFormMessages.IdTypeDriversLicense, value: KycIdentificationType.DRIVERS_LICENSE },
  { message: MemberCertificationFormMessages.IdTypePassport, value: KycIdentificationType.PASSPORT },
  { message: MemberCertificationFormMessages.IdTypeGovernmentId, value: KycIdentificationType.GOVERNMENT_ID},
];

type State = MethodError & {
  currentStep: number;
};

const initialState = Object.assign(initialFormState, { currentStep: 1 });

// tslint:disable no-any
interface WithHandlers {
  handleIdTypeSelectOnChange: (value: SelectValue) => void;
  handleDocFrontUploadChange: (file: RcFile) => any;
  handleDocBackUploadChange: (file: RcFile) => any;
  handleSelfieUploadChange: (file: RcFile) => any;
  handleRemoveImage: (field: string) => void;
}
// tslint:enable no-any

type WithFormikProps =
  WithStateProps<State> &
  WithSubmitKycApplicantProps &
  FormikProps<KycApplicantFormValues> &
  RouteComponentProps<{}>;

type WithHandlersProps =
  InjectedIntlProps &
  WithStateProps<State> &
  FormikProps<KycApplicantFormValues>;

type Props =
  FormikProps<KycApplicantFormValues> &
  InjectedIntlProps &
  WithStateProps<State> &
  WithHandlers;

const MemberCertificationFormComponent: React.SFC<Props> = (props) => {
  const {
    handleIdTypeSelectOnChange,
    handleDocFrontUploadChange,
    handleDocBackUploadChange,
    handleSelfieUploadChange,
    handleRemoveImage,
    state,
    values,
    isSubmitting,
    intl: { formatMessage }
  } = props;

  const boldOne = (
    <strong style={{ color: themeTypography.textColorDark }}>
      <FormattedMessage {...MemberCertificationFormMessages.UserImageUploadHelperTextBoldOne}/>
    </strong>
  );

  const boldTwo = (
    <strong style={{ color: themeTypography.textColorDark }}>
      <FormattedMessage {...MemberCertificationFormMessages.UserImageUploadHelperTextBoldTwo}/>
    </strong>
  );

  return (
    <Form style={{ marginTop: 15 }}>
      {state.methodError && <FormMethodError methodError={state.methodError}/>}

      {state.currentStep >= 1 &&
      <FormControlWrapper
        title={MemberCertificationFormMessages.IdTypeSelectionLabel}
        description={(
          <React.Fragment>
            <FormattedMessage {...MemberCertificationFormMessages.IdTypeSelectionHelperTextOne}/>
          </React.Fragment>
        )}
        isComplete={state.currentStep >= 2}
      >
        <FormControl {...selectColProps}>
          <Select
            onChange={handleIdTypeSelectOnChange}
            placeholder={formatMessage(MemberCertificationFormMessages.SelectPlaceholder)}
            size="large"
          >
            {idTypeOptions.map(idType => (
              <Select.Option key={idType.value} value={idType.value}>
                <FormattedMessage {...idType.message}/>
              </Select.Option>
            ))}
          </Select>
        </FormControl>

        {values.kycIdentificationType &&
        <UploadWrapper>
          <DragAndDropField
            name={DOC_FRONT_IMAGE}
            accept={`${sharedAcceptedFileTypes},.pdf`}
            dndTitle={getIdTypeMessage(props, MemberCertificationFormMessages.IdTypeSelectionFrontLabel)}
            iconType="idFront"
            fileList={values.docFrontImage ? [values.docFrontImage] as RcFile[] : undefined}
            showUploadList={!!values.docFrontImage}
            beforeUpload={handleDocFrontUploadChange}
            onRemove={() => handleRemoveImage(DOC_FRONT_IMAGE)}
            formFieldStyle={{ marginBottom: 0 }}
          />

          {values.kycIdentificationType !== KycIdentificationType.PASSPORT &&
          <DragAndDropField
            name={DOC_BACK_IMAGE}
            accept={`${sharedAcceptedFileTypes},.pdf`}
            dndTitle={getIdTypeMessage(props, MemberCertificationFormMessages.IdTypeSelectionBackLabel)}
            iconType="idBack"
            fileList={values.docBackImage ? [values.docBackImage] as RcFile[] : undefined}
            showUploadList={!!values.docBackImage}
            beforeUpload={handleDocBackUploadChange}
            onRemove={() => handleRemoveImage(DOC_BACK_IMAGE)}
            formFieldStyle={{ marginBottom: 0 }}
          />}
        </UploadWrapper>}
      </FormControlWrapper>}

      {state.currentStep >= 2 &&
      <FormControlWrapper
        title={MemberCertificationFormMessages.UserImageUploadLabel}
        description={(
          <FormattedMessage
            {...MemberCertificationFormMessages.UserImageUploadHelperText}
            values={{ boldOne, boldTwo }}
          />
        )}
        isComplete={state.currentStep >= 3}
      >
        <UploadWrapper style={{ marginTop: 10 }}>
          <SelfieExample/>

          <DragAndDropField
            name={LIVE_PHOTO_IMAGE}
            accept={sharedAcceptedFileTypes}
            dndTitle={<FormattedMessage {...MemberCertificationFormMessages.UserImageDragLabel}/>}
            iconType="selfie"
            fileList={values.livePhotoImage ? [values.livePhotoImage] as RcFile[] : undefined}
            showUploadList={!!values.livePhotoImage}
            formFieldStyle={{ maxWidth: 650, width: '100%', marginBottom: 0 }}
            beforeUpload={handleSelfieUploadChange}
            onRemove={() => handleRemoveImage(LIVE_PHOTO_IMAGE)}
          />
        </UploadWrapper>

      </FormControlWrapper>}

      {state.currentStep >= 2 &&
      <Button
        type="primary"
        size="large"
        htmlType="submit"
        loading={isSubmitting}
        style={{ marginBottom: 25 }}
      >
        <FormattedMessage {...SharedComponentMessages.ConfirmAndSubmit}/>
      </Button>}
    </Form>
  );
};

function getIdTypeMessage (props: Props, message: FormattedMessage.MessageDescriptor): React.ReactNode {
  const { values } = props;

  let idType;
  if (!values.kycIdentificationType) {
    return;
  }

  switch (values.kycIdentificationType) {
    case KycIdentificationType.DRIVERS_LICENSE:
      idType = <FormattedMessage {...MemberCertificationFormMessages.IdTypeDriversLicense}/>;
      break;
    case KycIdentificationType.PASSPORT:
      idType = <FormattedMessage {...MemberCertificationFormMessages.IdTypePassport}/>;
      break;
    case KycIdentificationType.GOVERNMENT_ID:
      idType = <FormattedMessage {...MemberCertificationFormMessages.IdTypeGovernmentId}/>;
      break;
    default:
      throw new Error('getIdTypeMessage: unknown id type');
  }

  return <FormattedMessage {...message} values={{ idType }}/>;
}

function applyFileToFormikState (props: WithHandlersProps, file: RcFile, fieldName: string) {
  const { setFieldValue, setErrors, setTouched, intl: { formatMessage } } = props;

  setTouched({ [fieldName]: false });
  const isFileSizeTooLarge = file.size > fileSizeMax;

  if (isFileSizeTooLarge) {
    setTouched({ [fieldName]: true });
    setTimeout(() =>
      setErrors({ [fieldName]: formatMessage(MemberCertificationFormMessages.FileSizeTooLargeErrorMsg)}),
      50
    );
    return false;
  }

  file.uid = fieldName;
  setFieldValue(fieldName, file);

  return true;
}

export const MemberCertificationForm = compose(
  withSubmitKycApplicant,
  withRouter,
  withState<State>(initialState),
  injectIntl,
  withFormik<WithFormikProps, KycApplicantFormValues>({
    ...kycApplicantFormikUtil,
    handleSubmit:  async (values, { props, setErrors, setSubmitting }) => {
      const { setState, isSubmitting, submitKycApplicant, history } = props;

      if (
        isSubmitting ||
        !values.kycIdentificationType ||
        !values.docFrontImage ||
        !values.livePhotoImage
      ) {
        return;
      }

      setState(ss => ({ ...ss, methodError: null }));

      try {
        const input: KycApplicationInput = {
          kycIdentificationType: values.kycIdentificationType
        };
        const docFrontImage = values.docFrontImage;
        // let's make sure we don't have a value for docBackImage if we're have a passport id type
        const docBackImage = values.kycIdentificationType !== KycIdentificationType.PASSPORT ?
          values.docBackImage :
          undefined;
        const livePhotoImage = values.livePhotoImage;
        const fileList = [docFrontImage, docBackImage, livePhotoImage]
          .filter(file => file !== undefined) as File[];
        const bodySerializer = buildBodySerializerForKycApplicantMutation(fileList);

        await submitKycApplicant(input, bodySerializer);

        history.push(WebRoute.MemberCertification);
      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  withHandlers<WithHandlersProps, {}>({
    handleIdTypeSelectOnChange: (props) => (value: SelectValue) => {
      const { setFieldValue, setState, state, values } = props;
      const isPassportIdType = value === KycIdentificationType.PASSPORT;
      let currentStep: number;

      setFieldValue(KYC_IDENTIFICATION_TYPE, value);

      // if the user decides to change the selected option - we potentially need to update the current step accordingly
      if (isPassportIdType) {
        currentStep = !!values.docFrontImage ? 2 : 1;
      } else {
        currentStep = !!values.docFrontImage && !!values.docBackImage ? 2 : 1;
      }

      // if the component states currentStep doesn't match then update state.currentStep
      if (state.currentStep !== currentStep) {
        setState(ss => ({ ...ss, currentStep }));
      }
    },
    handleDocFrontUploadChange: (props) => (file: RcFile) => {
      const { values, setState } = props;

      const updateFormState = applyFileToFormikState(props, file, DOC_FRONT_IMAGE);

      if (!updateFormState) {
        return false;
      }

      if (values.kycIdentificationType === KycIdentificationType.PASSPORT || !!values.docBackImage) {
        setState(ss => ({ ...ss, currentStep: 2 }));
      }

      return false;
    },
    handleDocBackUploadChange: (props) => (file: RcFile) => {
      const { setState, values } = props;

      const updateFormState = applyFileToFormikState(props, file, DOC_BACK_IMAGE);

      if (!updateFormState) {
        return false;
      }

      if (!!values.docFrontImage) {
        setState(ss => ({ ...ss, currentStep: 2 }));
      }

      return false;
    },
    handleSelfieUploadChange: (props) => (file: RcFile) => {
      const { setState } = props;

      const updateFormState = applyFileToFormikState(props, file, LIVE_PHOTO_IMAGE);

      if (!updateFormState) {
        return false;
      }

      setState(ss => ({ ...ss, currentStep: 3 }));

      return false;
    },
    handleRemoveImage: (props) => (field: string) => {
      const { setState, setFieldValue, setTouched } = props;

      let currentStep: number;
      setTouched({ [field]: false });
      setFieldValue(field, undefined);

      if (field === DOC_FRONT_IMAGE || field === DOC_BACK_IMAGE) {
        currentStep = 1;
      } else {
        currentStep = 2;
      }

      setState(ss => ({ ...ss, currentStep }));
    }
  })
)(MemberCertificationFormComponent) as React.ComponentClass<{}>;

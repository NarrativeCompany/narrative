import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { RouteComponentProps } from 'react-router';
import { Form, FormikProps, FormikState, withFormik } from 'formik';
import { Col, Row } from 'antd';
import { ColProps, RowProps } from 'antd/lib/grid';
import { SEO } from '../../shared/components/SEO';
import { Carousel, carouselRef } from '../../shared/components/Carousel';
import { RegisterWelcome } from './components/RegisterWelcome';
import { RegisterForm } from './components/RegisterForm';
import { NichesOfInterest } from './components/NichesOfInterest';
import { RegisterSuccess } from './components/RegisterSuccess';
import { SEOMessages } from '../../shared/i18n/SEOMessages';
import { validateEmail } from '../../shared/utils/validateEmail';
import { scrollToTop } from '../../shared/utils/scrollUtils';
import * as moment from 'moment-timezone';
import {
  CarouselState,
  initialCarouselState,
  withCarouselController,
  WithCarouselControllerProps
} from '../../shared/containers/withCarouselController';
import {
  applyExceptionToState,
  initialFormState,
  MethodError,
  Niche,
  registerFormUtil,
  RegisterFormValues,
  RegisterUserInput,
  withRegisterUser,
  WithRegisterUserProps,
  withState,
  WithStateProps,
  withValidateRegisterUser,
  WithValidateRegisterUserProps
} from '@narrative/shared';
import styled from '../../shared/styled';
import { RecaptchaHandlers, resetGrecaptcha, withRecaptchaHandlers } from '../../shared/utils/recaptchaUtils';

const RegisterWrapper = styled.div`
  @media screen and (max-width: 767px) {
    .ant-col-lg-8 {
      display: none;
    }
  }
`;

const StyledRow = styled<RowProps>((props) => <Row {...props}/>)`
  @media screen and (max-width: 575px) {
    &.ant-row-flex {
      display: block;
    }
  }
`;

const StyledCol = styled<ColProps>((props) => <Col {...props}/>)`
  min-height: 100%;
`;

const ContentWrapper = styled.div`
  padding: 60px 60px 0;
  
  @media screen and (max-width: 767px) {
    padding: 40px 30px 0;
  }
  
  @media screen and (max-width: 539px) {
    padding: 40px 20px 0;
  }
`;

type State =
  CarouselState &
  MethodError & {
  isSuccessfullyRegistered: boolean;
  selectedNiches: Niche[];
  isValidatingRegisterUser: boolean;
  isStepOneVerified: boolean;
};
const initialState: State = Object.assign(initialCarouselState, {
  isSuccessfullyRegistered: false,
  selectedNiches: [],
  isValidatingRegisterUser: false,
  isStepOneVerified: false,
  ...initialFormState
});

interface WithHandlers extends RecaptchaHandlers {
  handleAddSelectedNiche: (niche: Niche) => void;
  handleRemoveSelectedNiche: (niche: Niche) => void;
  handleValidateRegisterUser: () => void;
}

type WithFormikProps =
  WithStateProps<State> &
  RouteComponentProps<{emailAddress?: string}> &
  FormikProps<RegisterFormValues> &
  WithRegisterUserProps;

type WithHandlerProps =
  WithStateProps<State> &
  FormikProps<RegisterFormValues> &
  WithCarouselControllerProps &
  WithValidateRegisterUserProps;

type Props =
  WithStateProps<State> &
  FormikProps<RegisterFormValues> &
  WithCarouselControllerProps &
  WithHandlers;

const Register: React.SFC<Props> = (props) => {
  const {
    state,
    setState,
    handleAddSelectedNiche,
    handleRemoveSelectedNiche,
    handleValidateRegisterUser,
    handleRecaptchaVerifyCallback,
    handleRecaptchaExpiredCallback,
    isSubmitting,
    values,
    errors,
    touched
  } = props;

  const recaptchaError = touched.recaptchaResponse && errors.recaptchaResponse ?
    errors.recaptchaResponse as string :
    undefined;

  return (
    <RegisterWrapper>
      <SEO
        title={SEOMessages.RegisterTitle}
        description={SEOMessages.HomeAndRegisterDescription}
      />

      {state.isSuccessfullyRegistered &&
      <RegisterSuccess userEmailAddress={values.emailAddress}/>}

      {!state.isSuccessfullyRegistered &&
      <StyledRow type="flex">
        <StyledCol xl={9} lg={8} md={8}>
          <RegisterWelcome/>
        </StyledCol>

        <Col xl={14} lg={16} md={16} sm={24}>
          <ContentWrapper>
            <Form>
              <Carousel ref={carouselRef}>
                <RegisterForm
                  onRegisterSuccess={(userEmailAddress) => setState(ss => ({ ...ss, userEmailAddress }))}
                  onNextClick={handleValidateRegisterUser}
                  handleRecaptchaVerifyCallback={handleRecaptchaVerifyCallback}
                  handleRecaptchaExpiredCallback={handleRecaptchaExpiredCallback}
                  recaptchaError={recaptchaError}
                  methodError={state.methodError}
                  isValidating={state.isValidatingRegisterUser}
                />

                <NichesOfInterest
                  onAddSelectedNiche={handleAddSelectedNiche}
                  onRemoveSelectedNiche={handleRemoveSelectedNiche}
                  selectedNiches={state.selectedNiches}
                  methodError={state.methodError}
                  isSubmitting={isSubmitting}
                />
              </Carousel>
            </Form>
          </ContentWrapper>
        </Col>
      </StyledRow>}
    </RegisterWrapper>
  );
};

export default compose(
  withState<State>(initialState),
  withCarouselController,
  withValidateRegisterUser,
  withRegisterUser,
  withFormik<WithFormikProps, RegisterFormValues>({
    ...registerFormUtil,
    mapPropsToValues: (props) =>
      registerFormUtil.mapPropsToValues(validateEmail(props.match.params.emailAddress) || ''),
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, isSubmitting } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const timeZone = moment.tz.guess();
        const input: RegisterUserInput = {
          ...values,
          timeZone,
        };

        await props.registerUser(input);
        setState(ss => ({ ...ss, isSuccessfullyRegistered: true }));
      } catch (e) {
        applyExceptionToState(e, setErrors, setState);
        scrollToTop();
      }

      setSubmitting(false);
    }
  }),
  withRecaptchaHandlers,
  withHandlers<WithHandlerProps, {}>({
    handleValidateRegisterUser: (props) => async () => {
      const {
        setState,
        setTouched,
        setFieldValue,
        validateRegisterUser,
        values,
        handleNextSlideClick,
        setErrors,
        validateForm,
      } = props;

      setState(ss => ({ ...ss, isValidatingRegisterUser: true }));

      /* todo: Create a utility for doing adhoc form validation #2641 */
      const formValidation = await validateForm();

      setTouched({
        emailAddress: true,
        password: true,
        username: true,
        displayName: true,
        hasAgreedToTos: true,
        recaptchaResponse: true,
        recaptchaToken: true,
      });

      // tslint:disable no-any
      const formErrors =
        Object.keys(formValidation as any).filter(key => key !== 'recaptchaResponse' && key !== 'recaptchaToken');
      // tslint:enable no-any

      if (formErrors.length) {
        setState(ss => ({ ...ss, isValidatingRegisterUser: false }));
        return;
      }

      try {
        const timeZone = moment.tz.guess();
        const input: RegisterUserInput = { ...values, timeZone };
        const res = await validateRegisterUser(input);
        setFieldValue('recaptchaToken', res.recaptchaToken);
        handleNextSlideClick();
      } catch (err) {
        resetGrecaptcha();
        applyExceptionToState(err, setErrors, setState);
      }

      setState(ss => ({ ...ss, isValidatingRegisterUser: false, isStepOneVerified: true }));
    },
    // bl: note that this must override the default from withRecaptchaHandlers because we have the multi-step
    // process to handle.
    handleRecaptchaExpiredCallback: (props) => () => {
      const { setFieldValue, setFieldTouched, state } = props;

      if (state.isStepOneVerified) {
        return;
      }

      setFieldTouched('recaptchaResponse');
      setFieldValue('recaptchaResponse', '');
    },
    handleAddSelectedNiche: (props) => (niche: Niche) => {
      const { setState, setFormikState } = props;

      setState(ss => ({
        ...ss,
        selectedNiches: [ ...ss.selectedNiches, niche ],
        methodError: null
      }));

      // tslint:disable-next-line no-any
      setFormikState((prevState: FormikState<any>) => ({
        ...prevState,
        values: {
          ...prevState.values,
          nichesToFollow: [
            ...prevState.values.nichesToFollow,
            niche.oid
          ]
        }
      }));
    },
    handleRemoveSelectedNiche: (props) => (niche: Niche) => {
      const { state, setState, setFormikState  } = props;

      const selectedNiches = state.selectedNiches.filter(n => n.oid !== niche.oid);
      const selectedNicheOids = selectedNiches.map(n => n.oid);

      setState(ss => ({ ...ss, selectedNiches }));

      // tslint:disable-next-line no-any
      setFormikState((prevState: FormikState<any>) => ({
        ...prevState,
        values: {
          ...prevState.values,
          nichesToFollow: selectedNicheOids
        }
      }));
    }
  })
)(Register);

import { FormikProps } from 'formik';
import { RecaptchaFormValues } from '@narrative/shared';
import { withHandlers } from 'recompose';

// bl: there are other methods, but this is the only one we use right now
// refer: https://developers.google.com/recaptcha/docs/display#javascript_api
interface GrecaptchaProps {
  reset: () => void;
}

export interface GrecaptchaGlobalProps {
  // bl: grecaptcha is the global object that is exposed once recaptcha has been loaded
  grecaptcha?: GrecaptchaProps;
}

export function resetGrecaptcha() {
  const grecaptcha = (window as GrecaptchaGlobalProps).grecaptcha;
  if (grecaptcha) {
    grecaptcha.reset();
  }
}

export interface RecaptchaHandlers {
  handleRecaptchaVerifyCallback: (response: string) => void;
  handleRecaptchaExpiredCallback: () => void;
}

export const withRecaptchaHandlers = withHandlers<FormikProps<RecaptchaFormValues>, RecaptchaHandlers>({
    handleRecaptchaVerifyCallback: (props) => (response: string) => {
      const { setFieldValue } = props;

      setFieldValue('recaptchaResponse', response);
    },
    handleRecaptchaExpiredCallback: (props) => () => {
      const { setFieldValue, setFieldTouched } = props;

      setFieldTouched('recaptchaResponse');
      setFieldValue('recaptchaResponse', '');
    }
});

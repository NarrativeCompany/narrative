import * as React from 'react';
import * as ReactRecaptcha from 'react-recaptcha';
import { branch, compose, renderComponent, withProps } from 'recompose';
import {
  withIncludeScript,
  withRecaptchaPublicKey,
  WithRecaptchaPublicKeyProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import { ContainedLoading } from './Loading';
import styled from '../styled';
import { CSSProperties } from 'react';

const ErrorMessage = styled.div`
  color: #f5222d;
`;

interface State {
  isRecaptchaLoaded?: boolean;
}

interface WithProps {
  publicKey: string;
}

type RecaptchaProps = ReactRecaptcha.RecaptchaProps;

interface ParentProps {
  error?: string;
  style?: CSSProperties;
}

type Props =
  ParentProps &
  RecaptchaProps &
  WithRecaptchaPublicKeyProps &
  WithProps;

interface RecaptchaGlobalProps {
  onRecaptchaLoaded?: () => void;
  narrativeRecaptchaLoaded?: boolean;
}

export const RecaptchaComponent: React.SFC<Props> = (props) => {
  const { theme, type, publicKey, error, style } = props;

  if (!publicKey) {
    return null;
  }

  return (
    <div style={style}>
      <ReactRecaptcha
        {...props}
        sitekey={publicKey}
        theme={theme || 'light'}
        type={type || 'image'}
      />
      {error && <ErrorMessage>{error}</ErrorMessage>}
    </div>
  );
};

export const Recaptcha = compose(
  withState<State>({}),
  // bl: it's pretty dumb that we are using withProps here, but i'm not sure of a better fit to just execute some code.
  withProps((props: WithStateProps<State>) => {
    const { setState } = props;

    const recaptchaGlobalProps = window as RecaptchaGlobalProps;
    if (!recaptchaGlobalProps.onRecaptchaLoaded) {
      // bl: need to define a globally-scoped function so we can show the loading spinner until it's done.
      // this will avoid a race condition that could prevent our verify callback from firing and breaking registration.
      recaptchaGlobalProps.onRecaptchaLoaded = (): void => {
        // bl: this global variable is needed so that we know when it's loaded for other instances of reCAPTCHA
        // that are loaded after the first one.
        recaptchaGlobalProps.narrativeRecaptchaLoaded = true;
        // bl: all this is needed for is to force a re-render.
        setState(ss => ({...ss, isRecaptchaLoaded: true}));
      };
    }
  }),
  withIncludeScript('https://www.google.com/recaptcha/api.js?onload=onRecaptchaLoaded'),
  withRecaptchaPublicKey,
  withProps((props: Props) => {
    const { recaptchaData } = props;

    const publicKey =
      recaptchaData &&
      recaptchaData.getRecaptchaPublicKey &&
      recaptchaData.getRecaptchaPublicKey.publicKey;

    return { publicKey };
  }),
  branch((props: Props) => {
    // bl: not using state here, since it's not needed. if reCAPTCHA is already loaded, then we'll have the global
    // variable set and ready to go. otherwise, once it loads, we'll do a setState to effectively force a re-render.
    return !(window as RecaptchaGlobalProps).narrativeRecaptchaLoaded || props.recaptchaData.loading;
  },
    renderComponent(() => <ContainedLoading/>)
  )
)(RecaptchaComponent) as React.ComponentClass<RecaptchaProps & ParentProps>;

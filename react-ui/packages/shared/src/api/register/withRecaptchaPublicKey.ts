import { graphql, GraphqlQueryControls, NamedProps } from 'react-apollo';
import { recaptchaPublicKeyQuery } from '../graphql/register/recaptchaPublicKeyQuery';
import { RecaptchaPublicKeyQuery } from '../../types';

export type WithRecaptchaPublicKeyProps =
  NamedProps<{recaptchaData: GraphqlQueryControls & RecaptchaPublicKeyQuery}, {}>;

export const withRecaptchaPublicKey = graphql<{}, RecaptchaPublicKeyQuery, {}>(recaptchaPublicKeyQuery, {
  name: 'recaptchaData'
});

import { compose, lifecycle, withProps } from 'recompose';
import { RouteComponentProps, withRouter } from 'react-router';
import {
  SuspendEmailInput,
  withValidateSuspendEmailAddress,
  WithValidateSuspendEmailAddressProps,
  WithValidateSuspendEmailAddressParentProps
} from '@narrative/shared';
import * as React from 'react';
import { SEO } from '../../shared/components/SEO';
import { FormattedMessage } from 'react-intl';
import { SuspendEmailMessages } from '../../shared/i18n/SuspendEmailMessages';
import { SuspendEmailButtons } from './components/SuspendEmailButtons';
import { fullPlaceholder, withLoadingPlaceholder } from '../../shared/utils/withLoadingPlaceholder';
import { openNotification } from '../../shared/utils/notificationsUtil';
import { WebRoute } from '../../shared/constants/routes';
import { MemberLink } from '../../shared/components/user/MemberLink';
import { AuthForm, AuthHeader, AuthWrapper } from '../../shared/styled/shared/auth';
import { Col, Row } from 'antd';
import { Logo } from '../../shared/components/Logo';
import { Block } from '../../shared/components/Block';

interface RouteProps extends SuspendEmailInput {
  userOid: string;
}

export interface SuspendEmailProps {
  userOid: string;
  input: SuspendEmailInput;
}

type Props = SuspendEmailProps &
  WithValidateSuspendEmailAddressProps;

const SuspendEmailComponent: React.SFC<Props> = (props) => {
  // jw: if we got into here that means that the parameters passed validation and we can use them to drive a simple UI.
  const { userOid, input, validationResult: { user } } = props;
  // jw: let's ensure the user knows which email address they are suspending.
  const { emailAddress } = input;

  const userLink = <MemberLink user={user} targetBlank={true} hideBadge={true} appendUsername={true} />;

  return (
    <React.Fragment>
      <SEO title={SuspendEmailMessages.PageTitle} />

      <AuthWrapper centerAll={true}>
        <AuthForm>
          <Row type="flex" align="middle" justify="center" style={{ marginBottom: 25, marginTop: 15 }}>
            <Col>
              <Logo/>
            </Col>
          </Row>

          <Row type="flex" align="middle" justify="center" style={{ marginBottom: 25 }}>
            <Col>
              <AuthHeader style={{textAlign: 'center'}}>
                <FormattedMessage {...SuspendEmailMessages.PageTitle} />
              </AuthHeader>
            </Col>
          </Row>

          <Block style={{ marginBottom: 20 }}>
            <FormattedMessage {...SuspendEmailMessages.SuspendEmailsDescription} values={{userLink, emailAddress}} />
          </Block>

          <SuspendEmailButtons userOid={userOid} input={input} />
        </AuthForm>
      </AuthWrapper>
    </React.Fragment>
  );
};

export default compose(
  // jw: the first thing we need to do is pull the params out of the path and set them up for use
  //     with the withValidateSuspendEmailAddress HOC.
  withRouter,
  withProps<WithValidateSuspendEmailAddressParentProps, RouteComponentProps<RouteProps>>(
    (props: RouteComponentProps<RouteProps>): WithValidateSuspendEmailAddressParentProps => {
      const { userOid, emailAddress, token } = props.match.params;

      return {
        userOid,
        input: { emailAddress: decodeURIComponent(emailAddress), token }
      };
    }
  ),
  // jw: calling this will cause a get request to the server to validate the properties we got above. If it fails that
  //     will result in a ApplicationError which will be presented to the user.
  withValidateSuspendEmailAddress,
  // jw: until it loads just show a loading placeholder.
  withLoadingPlaceholder(fullPlaceholder),
  withRouter,
  // jw: we need to intercept errors from the server and present them as a notification while redirecting to home since
  //     there is nothing the user can do about the error.
  lifecycle<WithValidateSuspendEmailAddressProps & RouteComponentProps, {}>({
    // tslint:disable-next-line object-literal-shorthand
    componentDidMount: async function () {
      const { validationResult, history } = this.props;

      if (validationResult.error) {
        await openNotification.updateFailed(undefined,
          {
            description: '',
            message: validationResult.error,
            duration: 0
          });

        history.push(WebRoute.Home);
      }
    }
  }),
) (SuspendEmailComponent) as React.ComponentClass<{}>;

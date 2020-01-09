import * as React from 'react';
import { compose, lifecycle } from 'recompose';
import { withRouter, RouteComponentProps } from 'react-router';
import { Divider } from 'antd';
import { FlexContainer } from '../../../shared/styled/shared/containers';
import { HeaderRightNav } from './HeaderRightNav';
import { HeaderAvatar } from './HeaderAvatar';
import { HeaderAuthButtons } from './HeaderAuthButtons';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../../shared/containers/withExtractedCurrentUser';

type Props =
  RouteComponentProps<{}> &
  WithExtractedCurrentUserProps;

const HeaderRightComponent: React.SFC<Props> = (props) => {
  const { currentUserLoading, currentUser, userAuthenticated } = props;

  return (
    <FlexContainer justifyContent="flex-end" alignItems="center">
      <HeaderRightNav/>

      <Divider type="vertical" style={{height: 25, margin: '0 20px'}}/>

      {currentUserLoading ? null : userAuthenticated && currentUser ?
      <HeaderAvatar currentUser={currentUser}/> :
      <HeaderAuthButtons/>}
    </FlexContainer>
  );
};

export const HeaderRight = compose(
  withRouter,
  withExtractedCurrentUser,
  lifecycle({
    // tslint:disable object-literal-shorthand
    componentDidUpdate: async function (prevProps: Props) {
      const { location, refetchCurrentUser } = this.props;

      const currentLocationPath =
        location &&
        location.pathname;

      const prevLocationPath =
        prevProps &&
        prevProps.location &&
        prevProps.location.pathname;

      if (currentLocationPath !== prevLocationPath && refetchCurrentUser) {
        await refetchCurrentUser()
          .catch(() => {
            // tslint:disable-next-line no-console
            console.info('Fetch current user failed in HeaderRight');
          });
      }
    }
  })
)(HeaderRightComponent) as React.ComponentClass<{}>;

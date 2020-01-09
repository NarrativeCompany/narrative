import styled, { css } from '../styled';
import { Button } from 'antd';
import * as React from 'react';
import { branch, compose, withHandlers, withProps } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { withState, WithStateProps, CurrentUserFollowedItem } from '@narrative/shared';
import { withLoginModalHelpers, WithLoginModalHelpersProps } from '../containers/withLoginModalHelpers';
import { NativeButtonProps } from 'antd/lib/button/button';

export interface FollowButtonParentHandlers {
  handleStartFollowing: () => void;
  handleStopFollowing: () => void;
}

interface ParentProps extends FollowButtonParentHandlers {
  followedItem?: CurrentUserFollowedItem;
  loading?: boolean;
}

interface State {
  isPerformingFollow?: boolean;
}

interface StyledFollowButtonProps extends NativeButtonProps {
  onClick: () => void;
  currentUserFollowing: boolean;
  disabled?: boolean;
}

interface Props extends State {
  currentUserFollowing: boolean;
  handleButtonClick: () => void;
  loading?: boolean;
}

const StyledFollowButton =
  styled<StyledFollowButtonProps>(({currentUserFollowing, ...rest}) => <Button {...rest}/>)`
    &.ant-btn,
    &.ant-btn:hover,
    &.ant-btn:focus,
    &.ant-btn:active {
      font-size: 12px;
      padding: 0 15px;
      text-align: center;
      border-radius: 20px;
      height: 25px;
      border-color: #E2E6EC;
      color: ${props => props.theme.primaryBlue}
    }
    
    ${props => props.currentUserFollowing && css`
      &.ant-btn,
      &.ant-btn:hover,
      &.ant-btn:focus,
      &.ant-btn:active {
        background-color: ${props.theme.followButtonActiveBg}
        border-color: transparent;
        color: #fff;
      }
    `}
  `;

const FollowButtonComponent: React.SFC<Props> = (props) => {
  const { currentUserFollowing, handleButtonClick, isPerformingFollow, loading } = props;

  const btnText = currentUserFollowing
    ? SharedComponentMessages.Following
    : SharedComponentMessages.Follow;

  return (
    <StyledFollowButton
      currentUserFollowing={currentUserFollowing}
      onClick={handleButtonClick}
      loading={isPerformingFollow || loading}
      disabled={loading}
    >
      {/*
        jw: if we are loading data for the button, then let's not include text, and just have the spinner inside until
            the data loads and we have something to say.
       */}
      {!loading && <FormattedMessage {...btnText}/>}
    </StyledFollowButton>
  );
};

export const FollowButton = compose(
  withProps((props: ParentProps) => {
    const { followedItem } = props;

    const currentUserFollowing: boolean = !!(followedItem && followedItem.followed);

    return { currentUserFollowing };
  }),
  withLoginModalHelpers,
  branch((props: WithLoginModalHelpersProps) => !!props.openLoginModal,
    // jw: here is the guest route: for guests let's just load the login modal when clicked
    withHandlers({
      handleButtonClick: (props: WithLoginModalHelpersProps) => async () => {
        const { openLoginModal } = props;

        // jw: even though the branch ensured we have this, it's still optional according to the props, so...
        if (openLoginModal) {
          openLoginModal();
        }
      }
    }),

    // jw: here is the registered user route, where we need to get a bit more complex
    compose(
      withState<State>({}),
      withHandlers({
        handleButtonClick: (props: ParentProps & WithStateProps<State> & Props) => async () => {
          const { currentUserFollowing, setState } = props;

          setState(ss => ({...ss, isPerformingFollow: true}));

          try {
            if (currentUserFollowing) {
              await props.handleStopFollowing();
            } else {
              await props.handleStartFollowing();
            }
          } finally {
            setState(ss => ({...ss, isPerformingFollow: undefined}));
          }
        }
      }),
      // jw: let's keep state out of the component, so move 'isPerformingFollow' down.
      withProps((props: WithStateProps<State>) => {
        const { state: { isPerformingFollow } } = props;

        return { isPerformingFollow };
      })
    )
  ),
)(FollowButtonComponent) as React.ComponentClass<ParentProps>;

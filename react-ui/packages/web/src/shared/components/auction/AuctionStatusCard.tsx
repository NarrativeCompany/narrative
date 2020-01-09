import * as React from 'react';
import styled, { css } from '../../styled';
import { Icon, Tooltip } from 'antd';
import { Card, CardColor, CardProps } from '../Card';
import { Paragraph } from '../Paragraph';
import { compose, withHandlers, withProps } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';
import { FormattedMessage } from 'react-intl';
import { ThemeColorType } from '../../styled/theme';
import { Heading } from '../Heading';

const MessageBody = styled(Paragraph)`
  position: relative;
`;

interface StyledCardProps extends CardProps {
  forTransition?: boolean;
}

// jw: if we are transitioning we want to slow down the effect to a full second so that its more clear to the user.
const StyledCard = styled<StyledCardProps>(({forTransition, ...cardProps}) => <Card {...cardProps} />)`
  ${props => props.forTransition 
    ? `transition: all 1s ease-in-out !important;`
    : null
  };
  > .ant-card-body {
    padding: 0;
  }
`;

interface TitleProps {
  color: ThemeColorType;
}

const TitleContainer = styled.div<TitleProps>`
  border-radius: 6px 6px 0 0;
  padding: 10px;
  ${p => css`background: ${p.theme[p.color]};`};
`;

const BodyContainer = styled.div`
  padding: 24px;
`;

interface State {
  color: CardColor;
  transitionToColor?: CardColor;
  forTransition?: boolean;
}

// jw: I considered just doing the state change directly in the component, but decided to keep this so that the business
//     logic that drives the UI could be better separated into the HOC. Allowing the Rendering Compoennt to remain
//     more purely focused on rendering.
interface Handlers {
  handleColorTransition: (toColor: CardColor) => void;
}

interface ParentProps extends State {
  title: FormattedMessage.MessageDescriptor;
  color: CardColor;
  transitionToColor?: CardColor;
  message?: React.ReactNode;
  info?: React.ReactNode;
  cardProps?: CardProps;
}

type Props = ParentProps &
  Handlers;

export const AuctionStatusCardComponnet: React.SFC<Props> = (props) => {
  const {
    color,
    title,
    transitionToColor,
    handleColorTransition,
    forTransition,
    message,
    children,
    info,
    cardProps
  } = props;

  // jw: if we were provided a color, then let's wait three seconds and then switch the color of this component to the
  //     once specified.
  if (transitionToColor) {
    setTimeout(() => {
      handleColorTransition(transitionToColor);
    }, 100);
  }

  return (
    <StyledCard forTransition={forTransition} style={{marginBottom: '20px'}} {...cardProps}>
      <TitleContainer color={color}>
        <Heading size={4} textAlign="center" color="white" noMargin={true}><FormattedMessage {...title} /></Heading>
      </TitleContainer>

      <BodyContainer>
        {children}
        {message &&
          <MessageBody textAlign="center" size="large">
            {message}
            {info &&
              <Tooltip title={info} placement="bottomRight">
                <Icon type="info-circle" style={{marginLeft: '5px'}} />
              </Tooltip>
            }
          </MessageBody>
        }
      </BodyContainer>
    </StyledCard>
  );
};

export const AuctionStatusCard = compose(
  withState<State>((props: ParentProps) => {
    // jw: let's pull the properties we are rendering from and place them into the state to initialize this.
    const { color, transitionToColor } = props;

    return { color, transitionToColor, forTransition: false };
  }),
  // jw: now, let's go ahead and render from the state
  withProps((props: WithStateProps<State>) => {
    const { state } = props;

    return {...state};
  }),
  withHandlers({
    handleColorTransition: (props: WithStateProps<State>) => (toColor: CardColor) => {
      const { setState } = props;

      // jw: since we are re-rendering to our transition color, let's set the color to that, and clear the
      //     transition color.
      const transitionToColor = undefined;
      const color = toColor;

      setState(ss => ({...ss, color, transitionToColor, forTransition: true}));
    }
  })
)(AuctionStatusCardComponnet) as React.ComponentClass<ParentProps>;

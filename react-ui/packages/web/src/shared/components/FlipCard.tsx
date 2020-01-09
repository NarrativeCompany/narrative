import * as React from 'react';
import styled from '../styled';
import { compose, withProps } from 'recompose';

const FlipCardWrapper = styled<{}, 'div'>('div')`
  width: 100%;
  position: relative;
`;

const FlipCardInner = styled<{isFlipped: boolean}, 'div'>('div')`
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: 0.5s;
  
  ${props => props.isFlipped && 'transform: rotateY(-180deg)'};
`;

const CardFace = styled<{isFlipped: boolean}, 'div'>('div')`
  backface-visibility: hidden;
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
`;

const CardFaceFront = styled<{isFlipped: boolean}>(CardFace)`
  transform: rotateY(0deg);
  ${props => !props.isFlipped && 'position: relative; z-index: 2;'};
`;

const CardFaceBack = styled<{isFlipped: boolean}>(CardFace)`
  transform: rotateY(-180deg);
  ${props => props.isFlipped && 'position: relative; z-index: 2;'};
`;

interface WithProps {
  cardFront: React.ReactNode;
  cardBack: React.ReactNode;
}

interface ParentProps {
  isFlipped: boolean;
  placeholder?: React.ReactNode;
  front: React.ReactNode;
  back: React.ReactNode;
}

type Props =
  ParentProps &
  WithProps;

export const FlipCardComponent: React.SFC<Props> = (props) => {
  const { isFlipped, cardFront, cardBack } = props;

  return (
    <FlipCardWrapper>
      <FlipCardInner isFlipped={isFlipped}>
        <CardFaceFront isFlipped={isFlipped}>
          {cardFront}
        </CardFaceFront>

        <CardFaceBack isFlipped={isFlipped}>
          {cardBack}
        </CardFaceBack>
      </FlipCardInner>
    </FlipCardWrapper>
  );
};

export const FlipCard = compose(
  withProps((props: Props) => {
    const { front, back, placeholder, isFlipped } = props;

    const cardFront = placeholder && isFlipped ? placeholder : front;
    const cardBack = placeholder && !isFlipped ? placeholder : back;

    return { cardFront, cardBack };
  })
)(FlipCardComponent) as React.ComponentClass<ParentProps>;

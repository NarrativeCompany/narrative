import * as React from 'react';
import { Carousel as AntCarousel } from 'antd';
import { CarouselProps as AntCarouselProps } from 'antd/lib/carousel';
import styled from '../styled';
import { ForwardRefExoticComponent, PropsWithoutRef, RefAttributes } from 'react';

// tslint:disable no-any
export const StyledCarousel =
  styled<
    CarouselProps &
    {refObject: CarouselRef}
  >(({refObject, ...rest}) => <AntCarousel ref={refObject} {...rest}/>)`
    // TODO: remove this if we don't find any side effects after commenting out
    //&.slick-slider .slick-active {
    //  z-index: 10;
    //}
    
    &.slick-slider .slick-track,
    &.slick-slider .slick-list  {
      transform: unset !important;
    }
  ` as any;
// tslint:enable no-any

export type CarouselRef = React.RefObject<AntCarousel>;
export const carouselRef: CarouselRef = React.createRef();

type CarouselProps = AntCarouselProps & {
  ref: CarouselRef;
};

export const Carousel:
  ForwardRefExoticComponent<PropsWithoutRef<CarouselProps> & RefAttributes<AntCarousel>> =
  React.forwardRef((props, ref) => {
  const { speed, effect, dots, accessibility, ...carouselProps } = props;

  return (
    <StyledCarousel
      {...carouselProps}
      refObject={ref}
      speed={speed || 500}
      effect={effect || 'fade'}
      dots={dots || false}
      accessibility={accessibility || false}
    >
      {props.children}
    </StyledCarousel>
  );
});

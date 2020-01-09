import { branch, compose, renderComponent, withHandlers } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';
import { carouselRef, CarouselRef } from '../components/Carousel';
import { scrollToTop } from '../utils/scrollUtils';

export interface CarouselState {
  currentSlide: number;
  carouselRef: CarouselRef;
}
export const initialCarouselState: CarouselState = {
  currentSlide: 0,
  carouselRef,
};

interface WithHandlers {
  handleNextSlideClick: () => void;
  handlePrevSlideClick: () => void;
  handleGoToSlide: (slideNumber: number) => void;
}

export type WithCarouselControllerProps =
  WithStateProps<CarouselState> &
  WithHandlers;

export const withCarouselController = compose(
  withState<CarouselState>(initialCarouselState),
  withHandlers({
    handleNextSlideClick: (props: WithStateProps<CarouselState>) => () => {
      const { state, setState } = props;
      setState(ss => ({...ss, currentSlide: ss.currentSlide + 1}));

      if (state.carouselRef.current) {
        state.carouselRef.current.next();
      }

      scrollToTop();
    },
    handlePrevSlideClick: (props: WithStateProps<CarouselState>) => () => {
      const { state, setState } = props;
      setState(ss => ({...ss, currentSlide: ss.currentSlide - 1}));

      if (state.carouselRef.current) {
        state.carouselRef.current.prev();
      }

      scrollToTop();
    },
    handleGoToSlide: (props: WithStateProps<CarouselState>) => (slideNumber: number) => {
      const { state, setState } = props;

      setState(ss => ({...ss, currentSlide: slideNumber }));

      if (state.carouselRef.current) {
        state.carouselRef.current.goTo(slideNumber);
      }

      scrollToTop();
    }
  })
);

/** This is a helper HOC that can be used to control the rendering of slides in the carousel
 * import this in any direct child of the carousel component to prevent the rendering of the slide
 * unless the slide number === current active slide
 */
export const withRenderCurrentSlide = (slide: number) => branch<{currentSlide: number}>(
  (props => props.currentSlide !== slide),
  renderComponent(() => null)
);

import * as React from 'react';
import { RouteComponentProps } from 'react-router-dom';
import { compose, withHandlers } from 'recompose';
import { ViewWrapper } from '../../shared/components/ViewWrapper';
import { Card, CardProps } from '../../shared/components/Card';
import { Form, FormikProps, withFormik } from 'formik';
import { FlexContainer, FlexContainerProps } from '../../shared/styled/shared/containers';
import { SEO } from '../../shared/components/SEO';
import { NicheNameAndDescStep } from './components/NicheNameAndDescStep';
import { SimilarNicheResultsStep } from './components/SimilarNicheResultsStep';
import { NicheConfirmationStep } from './components/NicheConfirmationStep';
import { NicheReviewStep } from './components/NicheReviewStep';
import { Carousel, carouselRef } from '../../shared/components/Carousel';
import { FormSteps } from './components/FormSteps';
import { FormMethodError } from '../../shared/components/FormMethodError';
import { SEOMessages } from '../../shared/i18n/SEOMessages';
import {
  CarouselState,
  initialCarouselState,
  withCarouselController,
  WithCarouselControllerProps
} from '../../shared/containers/withCarouselController';
import { WithExtractedCurrentUserProps } from '../../shared/containers/withExtractedCurrentUser';
import {
  applyExceptionToState,
  findSimilarNichesFormUtil,
  initialFormState,
  MethodError,
  Niche,
  NicheDetailsFormValues as FindSimilarNichesFormValues,
  Referendum,
  SimilarNichesInput,
  withSimilarNiches,
  WithSimilarNichesProps,
  withState,
  WithStateProps
} from '@narrative/shared';
import styled from '../../shared/styled';
import { withPermissionsCardInterceptor } from '../../shared/containers/withPermissionsCardInterceptor';
import { RevokeReasonMessages } from '../../shared/i18n/RevokeReasonMessages';

const SuggestNicheWrapper = styled(FlexContainer)<FlexContainerProps>`
  background: transparent;
  height: 100%;
`;

const CardWrapper = styled.div`
  max-width: 1140px;
  width: 100%;
  height: 100%;
  margin: 0 auto;
`;

const StepCard = styled<CardProps>(Card)`
  min-height: 790px;
`;

type State = MethodError &
  CarouselState & {
  similarNiches?: Niche[];
  referendum?: Referendum;
};

const initialState: State = {
  ...initialCarouselState,
  ...initialFormState
};

interface WithHandlers {
  handleNicheCreated: (referendum: Referendum) => void;
  handleEditDetailClick: () => void;
}

type Props =
  WithSimilarNichesProps &
  WithExtractedCurrentUserProps &
  WithCarouselControllerProps &
  WithStateProps<State> &
  FormikProps<FindSimilarNichesFormValues> &
  RouteComponentProps<{}> &
  WithHandlers;

const SuggestNicheComponent: React.SFC<Props> = (props) => {
  const {
    state,
    values,
    handleNextSlideClick,
    handlePrevSlideClick,
    handleNicheCreated,
    handleEditDetailClick,
    currentUser,
    isSubmitting,
    setFieldValue
  } = props;

  return (
    <ViewWrapper>
      <SuggestNicheWrapper column={true}>
        <SEO
          title={SEOMessages.SuggestNicheTitle}
          description={SEOMessages.SuggestNicheDescription}
        />

        <CardWrapper>
          <StepCard hoverable={true}>
            <FormMethodError methodError={state.methodError}/>

            <FormSteps
              current={props.state.currentSlide}
              stepsContent={(
                <Carousel ref={carouselRef}>
                  <Form>
                    <NicheNameAndDescStep isSubmitting={isSubmitting} setFieldValue={setFieldValue}/>
                  </Form>

                  <SimilarNicheResultsStep
                    onClick={handleNextSlideClick}
                    onPrevClick={handlePrevSlideClick}
                    similarNiches={state.similarNiches}
                  />

                  <NicheConfirmationStep
                    onPrevClick={handlePrevSlideClick}
                    onNicheCreated={handleNicheCreated}
                    onEditDetailsClick={handleEditDetailClick}
                    name={values.name}
                    description={values.description}
                    currentUser={currentUser}
                  />

                  <NicheReviewStep
                    currentUser={currentUser}
                    referendum={state.referendum}
                  />
                </Carousel>
              )}
            />
          </StepCard>
        </CardWrapper>
      </SuggestNicheWrapper>
    </ViewWrapper>
  );
};

export default compose<Props, {}>(
  withPermissionsCardInterceptor(
    'suggestNiches',
    RevokeReasonMessages.SuggestNiche,
    RevokeReasonMessages.SuggestNicheTimeout
  ),
  withSimilarNiches,
  withState<State>(initialState),
  withCarouselController,
  withFormik<Props, FindSimilarNichesFormValues >({
    ...findSimilarNichesFormUtil,
    handleSubmit: async (values, {props, setErrors, setSubmitting}) => {
      const { setState, findSimilarNiches, isSubmitting, handleNextSlideClick } = props;

      if (isSubmitting) {
        return;
      }

      setState(ss => ({...ss, methodError: null}));

      try {
        const input: SimilarNichesInput = { name: values.name, description: values.description };
        const similarNiches = await findSimilarNiches(input);

        setState(ss => ({ ...ss, similarNiches }));

        handleNextSlideClick();
      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      setSubmitting(false);
    }
  }),
  withHandlers({
    handleNicheCreated: (props: Props) => (referendum: Referendum) => {
      const { setState, handleNextSlideClick } = props;

      setState(ss => ({
        ...ss,
        referendum
      }));

      handleNextSlideClick();
    },
    handleEditDetailClick: (props: Props) => () => {
      const { handleGoToSlide } = props;

      handleGoToSlide(0);
    }
  })
)(SuggestNicheComponent) as React.ComponentClass<{}>;

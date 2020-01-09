import { branch, compose, renderComponent, withProps } from 'recompose';
import {
  ErrorState,
  withErrorState,
  isGraphQLObjectEmpty
} from '@narrative/shared';
import { WithErrorStateProps } from '@narrative/shared';

export interface WithExtractedErrorStateProps {
  isError: boolean;
  errorState: ErrorState;
}

export const withExtractedErrorState = compose(
  withErrorState,
  branch((props: WithErrorStateProps) => props.errorStateData && props.errorStateData.loading,
    renderComponent(() => null)
  ),
  withProps((props: WithErrorStateProps) => {
    const { errorStateData } = props;

    const isError =
      errorStateData &&
      errorStateData.errorState &&
      !isGraphQLObjectEmpty(errorStateData.errorState);

    // If errorState is "empty", leave errorState prop undefined
    const errorState =
      isError &&
      errorStateData.errorState;

    return { isError, errorState };
  })
);

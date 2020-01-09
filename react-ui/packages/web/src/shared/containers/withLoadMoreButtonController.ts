import { compose, withProps } from 'recompose';
import { LoadMoreButtonProps } from '../components/LoadMoreButton';
import { withState, WithStateProps } from '@narrative/shared';

export interface WithLoadMoreButtonControllerParentProps {
  loadMoreWhenViewportWithin?: number;
  loadMore?: () => void;
  loadMoreUrl?: string;
}

interface State {
  isLoadingMore?: boolean;
}

export interface WithLoadMoreButtonControllerProps {
  loadMoreButtonProps?: LoadMoreButtonProps;
}

type Props =
  WithLoadMoreButtonControllerParentProps &
  WithStateProps<State>;

export const withLoadMoreButtonController = compose(
  withState<State>({}),
  withProps((props: Props) => {
    const { loadMore, loadMoreUrl, loadMoreWhenViewportWithin } = props;

    let loadMoreButtonProps: LoadMoreButtonProps | undefined;

    // jw: we only want to include a loadMoreButton if we have a load more function
    if (loadMore) {
      const { setState, state: { isLoadingMore } } = props;

      loadMoreButtonProps = {
        isLoadingMore,
        loadMoreUrl,
        // jw: let's load more when the button is within 300px of being on screen
        loadWhenViewportWithin: loadMoreWhenViewportWithin || 300,
        loadMore: async () => {
          // jw: only load more the first time we are called. The proximity sensor is kinda aggressive and can trigger
          //     again while we are still waiting on the current results.
          if (isLoadingMore) {
            return;
          }

          setState(ss => ({ ...ss, isLoadingMore: true }));
          try {
            await loadMore();

          } finally {
            setState(ss => ({ ...ss, isLoadingMore: undefined }));
          }
        }
      };
    }

    return { loadMoreButtonProps };
  })
);

import * as React from 'react';
import { compose, withHandlers } from 'recompose';
import { withState, WithStateProps } from '@narrative/shared';
import { Button } from './Button';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
import { FlexContainer } from '../styled/shared/containers';

/*
  DO NOT USE
  jw: this is a first stab at load more functionality from me from when I was first learning typescript and react. it is
      probably not the best way to handle load more, and we are shifting more towards a loadMoreFunction approach where
      the apollo query handles loading more results and merging them into existing data. Should use that instead.
      see: withLoadMoreButtonController.ts and contentStreamUtils.ts for a better idea of what I am talking about.
 */

// jw: we will need a state, and all that tracks is whether we should load more when we render.
interface State {
  loadMore: boolean;
}

interface WithHandlers {
  triggerLoadMore: () => void;
}

export interface LoadMoreButtonHandlers {
  // tslint:disable-next-line
  fetchMoreItems: () => React.ReactElement<any>;
}

interface ParentProps extends LoadMoreButtonHandlers {
  alignLeft?: boolean;
}

type Props = WithStateProps<State> &
  ParentProps &
  WithHandlers;

const LoadMoreButtonComponent: React.SFC<Props> = (props) => {
  const { fetchMoreItems, alignLeft, triggerLoadMore, state: {loadMore} } = props;

  if (loadMore) {
    return fetchMoreItems();
  }

  return (
    <FlexContainer justifyContent={alignLeft ? 'flex-start' : 'center'}>
      <Button onClick={triggerLoadMore} type="primary">
        <FormattedMessage {...SharedComponentMessages.LoadMore} />
      </Button>
    </FlexContainer>
  );
};

export const LegacyLoadMoreButton = compose(
  withState<State>({loadMore: false}),
  withHandlers({
    triggerLoadMore: (props: WithStateProps<State>) => () => {
      props.setState({loadMore: true});
    }
  })
)(LoadMoreButtonComponent) as React.ComponentClass<ParentProps>;

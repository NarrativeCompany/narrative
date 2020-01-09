import * as React from 'react';
import { FlexContainer } from '../styled/shared/containers';
import { Button } from './Button';
import { FormattedMessage } from 'react-intl';
import { SharedComponentMessages } from '../i18n/SharedComponentMessages';
// jw: for some reason modules I've been adding via yarn are getting index files that do not have classic export default
//     declarations, and so the object reference ends up breaking... This works, despite the tslint warning, so
//     ignoring it.
// @ts-ignore
import ReactVisibilitySensor from 'react-visibility-sensor';

/**
 * INTERNAL RENDERER
 *
 * jw: to ease the external version of this, let's centralize the core button into a inner component so we can change
 *     how / when we render it more easily.
 */

interface Props {
  isLoadingMore?: boolean;
  loadMore: () => void;
  // jw: this is necessary for spiders so that infinit load more processes can still be indexed by search engines
  loadMoreUrl?: string;
}

const LoadMoreButtonComponent: React.SFC<Props> = (props) => {
  const { loadMore, loadMoreUrl, isLoadingMore } = props;

  return (
    <FlexContainer centerAll={true}>
      <Button size="large" onClick={loadMore} type="primary" loading={isLoadingMore} href={loadMoreUrl}>
        <FormattedMessage {...SharedComponentMessages.LoadMore} />
      </Button>
    </FlexContainer>
  );
};

/**
 * EXPORTED COMPONENT
 */

export interface LoadMoreButtonProps extends Props {
  loadWhenViewportWithin?: number;
}

export const LoadMoreButton: React.SFC<LoadMoreButtonProps> = (props) => {
  const { loadWhenViewportWithin, ...buttonProps } = props;

  if (loadWhenViewportWithin) {
    const { loadMore } = props;

    return (
      <ReactVisibilitySensor
        // jw: we want this to trigger immediately when we get within the specified range, we don't want the height of
        //     the button to throw off the calculations.
        partialVisibility={true}
        // jw: we don't want an initial call when the page renders.
        delayedCall={true}
        // jw: offset represents an offset from the viewport to trigger events. Think of it like an absolutely position
        //     from the viewport. We want to trigger visibility events a distance down from the bottom of the viewport
        //     so it is a negative number.
        offset={{bottom: -loadWhenViewportWithin}}
        // jw: the on change is pretty easy, only trigger when we become visible
        onChange={async (visible: boolean) => {
          if (visible) {
            await loadMore();
          }
        }}
      >
        <LoadMoreButtonComponent {...buttonProps}/>
      </ReactVisibilitySensor>
    );
  }

  return <LoadMoreButtonComponent {...buttonProps}/>;
};

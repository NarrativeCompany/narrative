import * as React from 'react';
import { scrollToUrlHashElement } from '../utils/scrollUtils';

interface UrlFragmentAnchorProps {
  id: string;
}

/**
 * jw: The point of this component is to give us a place to anchor to within the page from the URL fragment. It will add
 *     the provided id to the container, and then once rendered it will check to see if the URL fragment matches the id.
 *     If it does, then it will scroll the element into view.
 */
export const UrlFragmentAnchor: React.SFC<UrlFragmentAnchorProps> = (props) => {
  const { id, children } = props;

  return (
    <div id={id}>
      {children}

      {/* jw: scrollToUrlHashElement will internally short out if the location fragment does not match this id */}
      {scrollToUrlHashElement(id)}
    </div>
  );
};

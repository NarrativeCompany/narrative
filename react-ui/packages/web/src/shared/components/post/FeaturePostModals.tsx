import * as React from 'react';
import { Post } from '@narrative/shared';
import { FeaturePostModal, FeaturePostModalHandler } from './FeaturePostModal';
import { UnfeaturePostModal, UnfeaturePostModalHandler } from './UnfeaturePostModal';
import { FeaturePostTitleImageWarningModal } from './FeaturePostTitleImageWarningModal';

export interface SharedFeaturePostModalsProps {
  post?: Post;
  closeModalHandler: () => void;
  processing?: boolean;
}

// jw: I'm trying a different system for this. Normally we would have multiple properties for a modal (whether is is
//     visible, how to close it, handler for when something changes). Instead, I am going to just provide a universal
//     close handler, and then optional properties to handle the individual action of modals. If the handler is present
//     then we know the modal is visible, if it's not, then we know it's not.
export type FeaturePostModalsProps =
  SharedFeaturePostModalsProps &
  FeaturePostModalHandler &
  UnfeaturePostModalHandler;

export const FeaturePostModals: React.SFC<FeaturePostModalsProps> = (props) => {
  const { featurePostHandler, unfeaturePostHandler, ...sharedProps } = props;

  return (
    <React.Fragment>
      <FeaturePostModal
        featurePostHandler={featurePostHandler}
        {...sharedProps}
      />
      <UnfeaturePostModal
        unfeaturePostHandler={unfeaturePostHandler}
        {...sharedProps}
      />
      <FeaturePostTitleImageWarningModal
        {...props}
      />
    </React.Fragment>
  );
};

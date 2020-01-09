import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { PostQualityRating } from './PostQualityRating';
import { WithPostByIdProps } from '@narrative/shared';
import styled from '../../../shared/styled';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { PostAgeRating } from './PostAgeRating';

export type PostActionProps = WithPostByIdProps & {
  showRatingsDisabledModal?: () => void;
};

const ActionsWrapper = styled<FlexContainerProps>(FlexContainer)`
  margin-bottom: 50px;

  ${mediaQuery.lg_up`
    & > div:not(:first-child) {
      margin-left: 20px;
    }
  `}
  ${mediaQuery.md_down`
    flex-direction: column;
    align-items: center;
    
    & > div:not(:first-child) {
      margin-top: 50px;
    }
  `}
`;

export const PostActions: React.SFC<PostActionProps> = (props) => {
  return (
    <ActionsWrapper justifyContent="space-between">
      <PostQualityRating {...props} />

      <PostAgeRating {...props} />
    </ActionsWrapper>
  );
};

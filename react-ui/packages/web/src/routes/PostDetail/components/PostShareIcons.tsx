import * as React from 'react';
import { FlexContainer, FlexContainerProps } from '../../../shared/styled/shared/containers';
import { WithPostByIdProps } from '@narrative/shared';
import styled from '../../../shared/styled';
import { getShareButtons } from '../../../shared/components/ShareIcons';
import { mediaQuery } from '../../../shared/styled/utils/mediaQuery';
import { getPostUrl } from '../../../shared/utils/postUtils';

const IconsWrapper = styled<FlexContainerProps>(FlexContainer)`
  min-width: 250px;
  max-width: 300px;
  
  margin-bottom: 100px;
  
  ${mediaQuery.md_down`margin: 0 auto 50px;`}
`;

export const PostShareIcons: React.SFC<WithPostByIdProps> = (props) => {
  const { post,  } = props;
  const { prettyUrlString } = post;

  // jw: because we want the buttons to be directly in the IconsWrapper, I am forced to do something extremely silly.
  const shareButtons = getShareButtons({
    path: getPostUrl(prettyUrlString, post.oid),
    title: post.title,
    description: post.subTitle || undefined,
    buttonSize: 32
  });

  return (
    <IconsWrapper alignItems="center" justifyContent="space-between">
      {shareButtons.map((button, i) => (
        <React.Fragment key={i}>{button}</React.Fragment>
      ))}
    </IconsWrapper>
  );
};

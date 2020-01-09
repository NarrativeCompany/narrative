import * as React from 'react';
import { compose, withProps } from 'recompose';
import { PostWrapper } from '../../../shared/styled/shared/post';
import { WithPostByIdProps } from '@narrative/shared';

interface HTML {
  __html: string ;
}

interface WithProps {
  postBody: HTML;
}

type Props =
  WithPostByIdProps &
  WithProps;

export const PostBodyComponent: React.SFC<Props> = (props) => {
  const { postBody } = props;

  return (
    <PostWrapper style={{marginBottom: 50}} className="fr-view" itemProp="articleBody">
      <div dangerouslySetInnerHTML={postBody}/>
    </PostWrapper>
  );
};

export const PostBody = compose(
  withProps((props: WithPostByIdProps) => {
    const { postDetail } = props;

    const postBody = { __html: postDetail.body };

    return { postBody };
  })
)(PostBodyComponent) as React.ComponentClass<WithPostByIdProps>;

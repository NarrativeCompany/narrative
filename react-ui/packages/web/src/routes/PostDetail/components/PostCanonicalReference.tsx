import * as React from 'react';
import { WithPostByIdProps } from '@narrative/shared';
import { branch, compose, renderComponent } from 'recompose';
import { FormattedMessage } from 'react-intl';
import { Alert } from 'antd';
import { Link } from '../../../shared/components/Link';
import { PostDetailMessages } from '../../../shared/i18n/PostDetailMessages';

const PostCanonicalReferenceComponent: React.SFC<WithPostByIdProps> = (props) => {
  const { postDetail } = props;
  const elsewhereLink = (
    <Link.Anchor href={postDetail.canonicalUrl || undefined} target="_blank">
      <FormattedMessage {...PostDetailMessages.Elsewhere}/>
    </Link.Anchor>
  );
  return (
    <Alert
      type="info"
      message={<FormattedMessage {...PostDetailMessages.PostCanonicalLinkReference} values={{elsewhereLink}}/>}
      style={{ marginBottom: 20 }}
    />
  );
};

export const PostCanonicalReference = compose(
  branch((props: WithPostByIdProps) =>
    props.postDetailLoading || !props.postDetail || !props.postDetail.canonicalUrl,
    renderComponent(() => null)
  ),
)(PostCanonicalReferenceComponent) as React.ComponentClass<WithPostByIdProps>;

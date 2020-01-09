import * as React from 'react';
import { branch, compose, renderNothing, withHandlers } from 'recompose';
import { PostMessages } from '../../../shared/i18n/PostMessages';
import { ApprovePublicationPostModal } from '../../../shared/components/post/ApprovePublicationPostModal';
import { RouterProps, withRouter } from 'react-router';
import {
  withApprovePublicationPostController,
  WithApprovePublicationPostControllerParentProps,
  WithApprovePublicationPostControllerProps
} from '../../../shared/containers/withApprovePublicationPostController';
import { getPostUrl } from '../../../shared/utils/postUtils';
import {
  PublicationDetailsConnect,
  WithPublicationDetailsContextProps
} from '../../Publication/components/PublicationDetailsContext';
import { PostDetail } from '@narrative/shared';

interface ParentProps {
  postDetail: PostDetail;
}

type Props = WithApprovePublicationPostControllerProps &
  ParentProps;

type Handlers = Pick<WithApprovePublicationPostControllerParentProps, 'approveSuccessHandler'>;

const ApprovePublicationPostButtonComponent: React.SFC<Props> = (props) => {
  const { generateApprovePublicationPostButton, approvePublicationPostModalProps } = props;
  // bl: if we don't have the props or generator, then there's nothing to render, so short out.
  if (!approvePublicationPostModalProps || !generateApprovePublicationPostButton) {
    return null;
  }

  return (
    <React.Fragment>
      {generateApprovePublicationPostButton('large', PostMessages.ApproveThisPostNowButtonText)}
      <ApprovePublicationPostModal {...approvePublicationPostModalProps} />
    </React.Fragment>
  );
};

export const ApprovePublicationPostButton = compose(
  PublicationDetailsConnect,
  branch((props: WithPublicationDetailsContextProps): boolean => !props.publicationDetail,
    renderNothing
  ),
  withRouter,
  withHandlers<Props & RouterProps, Handlers>({
    approveSuccessHandler: (props) => (): void => {
      const { history, postDetail } = props;
      history.push(getPostUrl(postDetail.post.prettyUrlString, postDetail.oid));
    }
  }),
  withApprovePublicationPostController
)(ApprovePublicationPostButtonComponent) as React.ComponentClass<ParentProps>;
